package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EmberethBlaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtueOfCourage.class, EmberethBlaze.class, GrizzlyBears.class, Island.class, Shock.class})
class VirtueOfCourageTest extends BaseCardTest {

    @Test
    void adventureDealsTwoDamageAndExilesTheCard() {
        VirtueOfCourage card = new VirtueOfCourage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void noncombatDamageOffersExilingThatManyCardsAndPlayingThem() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var first = new GrizzlyBears();
        var second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    void enchantmentCanBeCastFromExileAfterItsAdventureResolves() {
        var card = new VirtueOfCourage();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Virtue of Courage");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void exiledLandCanBePlayedDuringTheMainPhase() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void unplayedCardsRemainExiledButPermissionExpiresAfterTheTurn() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
    }

    @Test
    void damageToYourselfDoesNotTriggerTheEnchantment() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void opponentsDamageSourceDoesNotTriggerYourEnchantment() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player2, List.of(new VirtueOfCourage()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void combatDamageDoesNotTriggerTheEnchantment() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void adventureCanDamageACreatureWithoutTriggeringTheEnchantment() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void exiledEnchantmentRequiresItsNormalManaCost() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var exiled = new VirtueOfCourage();
        harness.setLibrary(player1, List.of(exiled));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiled.getId()));
    }

    @Test
    void exilePermissionDoesNotAllowCastingAnEnchantmentDuringUpkeep() {
        harness.addToBattlefield(player1, new VirtueOfCourage());
        var exiled = new VirtueOfCourage();
        harness.setLibrary(player1, List.of(exiled));
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
    }
}
