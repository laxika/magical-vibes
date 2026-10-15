package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FeedTheCauldron;
import com.github.laxika.magicalvibes.cards.m.MinecartDaredevil;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NotDeadAfterAll.class, MinecartDaredevil.class, FeedTheCauldron.class})
class NotDeadAfterAllTest extends BaseCardTest {

    @Test
    void returnsTappedAndAttachesWickedRole() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        Permanent role = findPermanent(player1, "Wicked");

        assertThat(returned.isTapped()).isTrue();
        assertThat(role.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isFalse();
    }

    @Test
    void wickedRoleCausesOpponentToLoseLifeWhenItDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Minecart Daredevil");
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        destroy(player2, returned);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(countPermanents(player1, "Minecart Daredevil")).isZero();
        assertThat(countPermanents(player1, "Wicked")).isZero();
    }

    @Test
    void canTargetOnlyACreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MinecartDaredevil());
        harness.setHand(player1, List.of(new NotDeadAfterAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void creatureDyingInResponseDoesNotReturnOrCreateRole() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());
        Card creatureCard = creature.getCard();
        harness.setHand(player1, List.of(new NotDeadAfterAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());

        destroy(player2, creature);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(countPermanents(player1, "Minecart Daredevil")).isZero();
        assertThat(countPermanents(player1, "Wicked")).isZero();
    }

    @Test
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());
        Card creatureCard = creature.getCard();
        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, creature);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        assertThat(countPermanents(player1, "Minecart Daredevil")).isZero();
        assertThat(countPermanents(player1, "Wicked")).isZero();
    }

    @Test
    void resolvingSpellDoesNotImmediatelyChangeTheCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());

        castOn(creature);

        assertThat(findPermanent(player1, "Minecart Daredevil")).isSameAs(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(countPermanents(player1, "Wicked")).isZero();
    }

    @Test
    void stolenCreatureReturnsToOwnerButRoleBelongsToTriggerController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MinecartDaredevil());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        castOn(creature);
        destroy(player2, creature);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Minecart Daredevil");
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(returned.isTapped()).isTrue();
        assertThat(role.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(countPermanents(player1, "Minecart Daredevil")).isZero();

        int ownerLife = gd.playerLifeTotals.get(player2.getId());
        int roleControllerLife = gd.playerLifeTotals.get(player1.getId());
        destroy(player1, returned);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(roleControllerLife);
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NotDeadAfterAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroy(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new FeedTheCauldron()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
