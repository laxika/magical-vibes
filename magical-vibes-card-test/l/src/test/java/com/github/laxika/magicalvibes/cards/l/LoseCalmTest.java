package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoseCalm.class, DragonScarredBear.class, Pacifism.class})
class LoseCalmTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of the target creature, untaps it, and grants haste and menace")
    void resolvesAllEffects() {
        Permanent target = addCreatureReady(player2, new DragonScarredBear());
        target.tap();

        castLoseCalm(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control, haste, and menace expire at cleanup")
    void effectsExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new DragonScarredBear());

        castLoseCalm(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player2, new DragonScarredBear());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        enchantment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new LoseCalm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Can untap and grant keywords to a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        target.tap();

        castLoseCalm(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Stolen creature can attack immediately and cannot be blocked by one creature")
    void hasteEnablesAttackAndMenaceRejectsOneBlocker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        addCreatureReady(player2, new DragonScarredBear());

        castLoseCalm(target);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(target.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Granted menace permits two creatures to block")
    void menaceAllowsTwoBlockers() {
        Permanent target = addCreatureReady(player2, new DragonScarredBear());
        Permanent firstBlocker = addCreatureReady(player2, new DragonScarredBear());
        Permanent secondBlocker = addCreatureReady(player2, new DragonScarredBear());

        castLoseCalm(target);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A target that leaves the battlefield before resolution receives no effects")
    void removedTargetDoesNotResolve() {
        Permanent target = addCreatureReady(player2, new DragonScarredBear());
        target.tap();
        castLoseCalm(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lose Calm");
    }

    private void castLoseCalm(Permanent target) {
        harness.setHand(player1, List.of(new LoseCalm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
    }
}
