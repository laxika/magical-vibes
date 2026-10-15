package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThorGuardianOfMidgard.class, GrizzlyBears.class, Island.class, Shock.class, ProdigalPyromancer.class, RayOfCommand.class})
class ThorGuardianOfMidgardTest extends BaseCardTest {

    @Test
    void noncombatDamageOffersExilingThatManyCardsAndPlayingThem() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var first = new GrizzlyBears();
        var second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(first.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(second.getId())).isEqualTo(player1.getId());
    }

    @Test
    void decliningTheTriggerLeavesTheLibraryUnchanged() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void exiledLandsCanBePlayedButDoNotGrantExtraLandPlays() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var first = new Island();
        var second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void playPermissionExpiresAfterTheTurnWhileTheCardRemainsExiled() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bear.getId());
    }

    @Test
    void damageToYourselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void anOpponentsDamageSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void damageToACreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void damageUsesTheSourcesCurrentControllerRatherThanTheAbilityController() {
        harness.addToBattlefield(player1, new ThorGuardianOfMidgard());
        var pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.castAndResolveInstant(player2, 0, pyromancer.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void combatDamageDoesNotTrigger() {
        addCreatureReady(player1, new ThorGuardianOfMidgard());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
