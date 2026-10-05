package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AegisTurtle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LavaSerpent;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NethroiApexOfDeath.class, GrizzlyBears.class, HillGiant.class,
        AegisTurtle.class, LavaSerpent.class, Mortivore.class})
class NethroiApexOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns any number of target creature cards with total power 10 or less")
    void mutatingReturnsCreatureCardsWithinTotalPowerLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card bear = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear, hillGiant, secondBear));

        triggerMutation(nethroi);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId(), hillGiant.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(bear.getId(), hillGiant.getId(), secondBear.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mutating rejects a target selection whose total power exceeds 10")
    void mutatingRejectsSelectionAboveTotalPowerLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        List<Card> hillGiants = List.of(new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant());
        harness.setGraveyard(player1, hillGiants);

        triggerMutation(nethroi);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, hillGiants.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 10");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1,
                hillGiants.subList(0, 3).stream().map(Card::getId).toList());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(hillGiants.get(3));
    }

    @Test
    @DisplayName("Mutating with no creature cards still creates an optional trigger")
    void mutatingWithNoCreatureCardsCreatesNoTargetsTrigger() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());

        triggerMutation(nethroi);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero-power cards can be returned in addition to ten power of creatures")
    void zeroPowerCardsDoNotConsumePowerAllowance() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card firstSerpent = new LavaSerpent();
        Card secondSerpent = new LavaSerpent();
        Card firstTurtle = new AegisTurtle();
        Card secondTurtle = new AegisTurtle();
        List<Card> targets = List.of(firstSerpent, secondSerpent, firstTurtle, secondTurtle);
        harness.setGraveyard(player1, targets);

        triggerMutation(nethroi);
        harness.handleMultipleCardsChosen(player1, targets.stream().map(Card::getId).toList());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Lava Serpent")).isEqualTo(2);
        assertThat(countPermanents(player1, "Aegis Turtle")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can choose no targets even when creatures are available")
    void mayChooseZeroTargets() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card turtle = new AegisTurtle();
        harness.setGraveyard(player1, List.of(turtle));

        triggerMutation(nethroi);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aegis Turtle");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(turtle);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing one target does not prevent the remaining legal target from returning")
    void returnsRemainingTargetWhenAnotherLeavesGraveyard() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card serpent = new LavaSerpent();
        Card turtle = new AegisTurtle();
        harness.setGraveyard(player1, List.of(serpent, turtle));

        triggerMutation(nethroi);
        harness.handleMultipleCardsChosen(player1, List.of(serpent.getId(), turtle.getId()));
        harness.setGraveyard(player1, List.of(turtle));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aegis Turtle");
        harness.assertNotOnBattlefield(player1, "Lava Serpent");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Characteristic-defined graveyard power counts when selecting targets")
    void rejectsTargetsWhoseCharacteristicDefinedPowerExceedsLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card mortivore = new Mortivore();
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(mortivore, giant));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        triggerMutation(nethroi);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(mortivore.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("If graveyard power grows above ten before resolution no targets return")
    void returnsNothingWhenCharacteristicDefinedPowerGrowsAboveLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card mortivore = new Mortivore();
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(mortivore, giant));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        triggerMutation(nethroi);
        harness.handleMultipleCardsChosen(player1, List.of(mortivore.getId(), giant.getId()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mortivore");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mortivore, giant);
    }

    @Test
    @DisplayName("Nethroi can be cast for its mutate cost targeting an owned non-Human")
    void canCastForMutateCost() {
        Permanent turtle = addCreatureReady(player1, new AegisTurtle());
        harness.setHand(player1, List.of(new NethroiApexOfDeath()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castWithAlternateCost(player1, 0, turtle.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting Nethroi normally does not trigger its graveyard return")
    void normalCastDoesNotReturnCreatures() {
        Card turtle = new AegisTurtle();
        harness.setGraveyard(player1, List.of(turtle));
        harness.setHand(player1, List.of(new NethroiApexOfDeath()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nethroi, Apex of Death");
        harness.assertNotOnBattlefield(player1, "Aegis Turtle");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(turtle);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void triggerMutation(Permanent nethroi) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, nethroi, List.of(nethroi.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSelfTriggeredAbilityTarget(gd));
    }
}
