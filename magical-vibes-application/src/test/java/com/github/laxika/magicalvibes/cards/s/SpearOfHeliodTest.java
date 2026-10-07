package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpearOfHeliod.class, GrizzlyBears.class, ProdigalSorcerer.class})
class SpearOfHeliodTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control, but not creatures controlled by an opponent")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new SpearOfHeliod());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys a creature that dealt damage to you this turn")
    void destroysCreatureThatDealtDamageToYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, spear), null, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sorcerer);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(sorcerer.getCard().getId()));
        assertThat(spear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that did not deal damage to you this turn")
    void cannotTargetCreatureThatDidNotDealDamageToYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, spear), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysCreatureThatDealtCombatDamageToYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(indexOf(player2, bears)));
        resolveCombat(player2);
        harness.assertLife(player1, 18);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, spear), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureThatDamagedAnotherPlayer() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, spear), null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnCreatureThatDamagedYou() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, spear), null, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sorcerer);
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
    }

    @Test
    void cannotActivateWithoutEnoughWhiteMana() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, spear), null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedSpear() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();
        spear.setTapped(true);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, spear), null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureThatOnlyDamagedACreature() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, bears.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, spear), null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureThatDamagedYouOnPreviousTurn() {
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new SpearOfHeliod());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, spear), null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spear.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
