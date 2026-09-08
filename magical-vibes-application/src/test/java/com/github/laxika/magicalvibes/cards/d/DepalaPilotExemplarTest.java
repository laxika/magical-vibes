package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DepalaPilotExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dwarves and creature Vehicles you control get +1/+1")
    void boostsDwarvesAndCreatureVehicles() {
        harness.addToBattlefield(player1, new DepalaPilotExemplar());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, creature("Dwarf", CardSubtype.DWARF));
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, artifactCreature("Vehicle", CardSubtype.VEHICLE));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, creature("Bear", CardSubtype.BEAR));
        Permanent noncreatureVehicle = harness.addToBattlefieldAndReturn(
                player1, artifact("Noncreature Vehicle", CardSubtype.VEHICLE));

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, noncreatureVehicle)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, noncreatureVehicle)).isZero();
    }

    @Test
    @DisplayName("When Depala becomes tapped, X pays and puts Dwarf and Vehicle cards into hand")
    void paysXAndRevealsMatchingCards() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Card dwarf = creature("Top Dwarf", CardSubtype.DWARF);
        Card vehicle = artifact("Top Vehicle", CardSubtype.VEHICLE);
        Card other = creature("Top Bear", CardSubtype.BEAR);
        Card deeperDwarf = creature("Deeper Dwarf", CardSubtype.DWARF);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(dwarf, vehicle, other, deeperDwarf));
        harness.addMana(player1, ManaColor.WHITE, 3);

        tapAndResolve(depala);

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(dwarf, vehicle);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(other, deeperDwarf);
        assertThat(deck).containsExactly(deeperDwarf, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing X=0 declines Depala's library ability")
    void choosingZeroDeclines() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Card dwarf = creature("Top Dwarf", CardSubtype.DWARF);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(dwarf);
        harness.addMana(player1, ManaColor.WHITE, 2);

        tapAndResolve(depala);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dwarf);
        assertThat(deck).containsExactly(dwarf);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card artifactCreature(String name, CardSubtype subtype) {
        Card card = creature(name, subtype);
        card.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        return card;
    }

    private static Card artifact(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
