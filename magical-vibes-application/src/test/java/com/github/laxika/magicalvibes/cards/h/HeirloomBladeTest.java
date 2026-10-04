package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeirloomBlade.class, GrizzlyBears.class, AirElemental.class, Forest.class, DoomBlade.class,
        NevinyrralsDisk.class})
class HeirloomBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("May reveal until a matching creature is found and put it into hand")
    void findsCreatureSharingTypeWithDyingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        AirElemental elemental = new AirElemental();
        GrizzlyBears matchingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, elemental, matchingCreature));

        killCreature(creature);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), elemental.getId());
    }

    @Test
    @DisplayName("Declining the reveal leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        GrizzlyBears matchingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, matchingCreature));

        killCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, matchingCreature);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void equipCostsOneManaAndMovesTheBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void unrevealedCardsStayAboveRandomlyBottomedCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest revealedLand = new Forest();
        AirElemental revealedCreature = new AirElemental();
        GrizzlyBears match = new GrizzlyBears();
        Forest unrevealedLand = new Forest();
        GrizzlyBears unrevealedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealedLand, revealedCreature, match,
                unrevealedLand, unrevealedCreature));

        killCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(match).doesNotContain(unrevealedCreature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unrevealedLand, unrevealedCreature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(revealedLand, revealedCreature);
    }

    @Test
    void noMatchingCreatureReturnsEntireLibraryWithoutAddingToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        AirElemental elemental = new AirElemental();
        harness.setLibrary(player1, List.of(forest, elemental));
        harness.setHand(player1, List.of());

        killCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, elemental);
    }

    @Test
    void equipmentControllerRevealsEvenWhenOpponentControlsEquippedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        GrizzlyBears match = new GrizzlyBears();
        Forest opponentTopCard = new Forest();
        harness.setLibrary(player1, List.of(match));
        harness.setLibrary(player2, List.of(opponentTopCard));

        killCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(match);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
    }

    @Test
    @CardUsed({HeirloomBlade.class, GrizzlyBears.class, NevinyrralsDisk.class})
    void triggersWhenBladeAndEquippedCreatureAreDestroyedSimultaneously() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blade.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        GrizzlyBears match = new GrizzlyBears();
        harness.setLibrary(player1, List.of(match));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(match);
    }

    private void killCreature(Permanent creature) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, creature.getId());
    }
}
