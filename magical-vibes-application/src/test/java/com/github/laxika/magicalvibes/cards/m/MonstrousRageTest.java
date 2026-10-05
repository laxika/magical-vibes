package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonstrousRage.class, GrizzlyBears.class, Mountain.class})
class MonstrousRageTest extends BaseCardTest {

    @Test
    void boostsTargetAndAttachesMonsterRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRage(target);

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canAttachMonsterRoleToAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castRage(target);

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = addCreatureReady(player1, new Mountain());
        harness.setHand(player1, List.of(new MonstrousRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void monsterRoleRemainsAfterTemporaryBoostExpires() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRage(target);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void secondRoleReplacesSameControllersOlderRoleButBoostsStack() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRage(target);
        Permanent oldRole = findPermanent(player1, "Monster");

        castRage(target);

        assertThat(findPermanents(player1, "Monster")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Monster");
        assertThat(newRole.getId()).isNotEqualTo(oldRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void rolesControlledByDifferentPlayersCanEnchantSameCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRage(target);
        Permanent firstRole = findPermanent(player1, "Monster");
        harness.setHand(player2, List.of(new MonstrousRage()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(findPermanents(player1, "Monster")).containsExactly(firstRole);
        assertThat(findPermanents(player2, "Monster")).hasSize(1);
        assertThat(findPermanent(player2, "Monster").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotCreateRoleWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrousRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monster")).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Rage");
    }

    private void castRage(Permanent target) {
        harness.setHand(player1, List.of(new MonstrousRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
