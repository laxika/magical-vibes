package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MishrasWorkshop;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.s.SwiftReconfiguration;
import com.github.laxika.magicalvibes.cards.t.ToolcraftExemplar;
import com.github.laxika.magicalvibes.cards.t.TrustyCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DepalaPilotExemplar.class, SkySkiff.class, ToolcraftExemplar.class,
        TrustyCompanion.class, SwiftReconfiguration.class, MishrasWorkshop.class})
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
        harness.setLibrary(player1, List.of(dwarf, vehicle, other, deeperDwarf));
        List<Card> deck = gd.playerDecks.get(player1.getId());
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
    @DisplayName("Choosing X=0 leaves mana and library unchanged")
    void choosingZeroLeavesLibraryUnchanged() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Card dwarf = creature("Top Dwarf", CardSubtype.DWARF);
        harness.setLibrary(player1, List.of(dwarf));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        tapAndResolve(depala);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dwarf);
        assertThat(deck).containsExactly(dwarf);
    }

    @Test
    void doesNotBoostItselfOrOpposingDwarves() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Permanent opponentDwarf = harness.addToBattlefieldAndReturn(player2, new ToolcraftExemplar());

        assertThat(gqs.getEffectivePower(gd, depala)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, depala)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentDwarf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentDwarf)).isEqualTo(1);
    }

    @Test
    void crewTapsDepalaAndBoostsVehicleAfterResolution() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new SkySkiff());
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        assertThat(depala.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(gqs.getEffectivePower(gd, skiff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skiff)).isEqualTo(4);
    }

    @Test
    void revealsOnlyAvailableCardsWhenXExceedsLibrarySize() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Card dwarf = new ToolcraftExemplar();
        Card vehicle = new SkySkiff();
        Card other = new TrustyCompanion();
        harness.setLibrary(player1, List.of(dwarf, vehicle, other));
        harness.addMana(player1, ManaColor.WHITE, 5);

        tapAndResolve(depala);
        harness.handleXValueChosen(player1, 5);

        assertThat(gd.playerHands.get(player1.getId())).contains(dwarf, vehicle).doesNotContain(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappingAnotherDwarfDoesNotTriggerDepala() {
        harness.addToBattlefield(player1, new DepalaPilotExemplar());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new ToolcraftExemplar());
        harness.addMana(player1, ManaColor.WHITE, 2);

        dwarf.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, dwarf));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void boostsItselfWhenReconfiguredAndCrewed() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SwiftReconfiguration());
        aura.setAttachedTo(depala.getId());
        harness.addToBattlefield(player1, new TrustyCompanion());
        harness.addToBattlefield(player1, new TrustyCompanion());

        assertThat(gqs.isCreature(gd, depala)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, depala)).isTrue();
        assertThat(gqs.getEffectivePower(gd, depala)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, depala)).isEqualTo(4);
    }

    @Test
    void artifactSpellOnlyManaCannotPayForTriggeredAbility() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        harness.addToBattlefield(player1, new MishrasWorkshop());
        harness.activateAbility(player1, 1, null, null);
        harness.addMana(player1, ManaColor.WHITE, 1);

        tapAndResolve(depala);

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(1);
    }

    @Test
    void nonmatchingRevealedCardsGoBelowUnrevealedCardsWithoutAReorderChoice() {
        Permanent depala = harness.addToBattlefieldAndReturn(player1, new DepalaPilotExemplar());
        Card first = new TrustyCompanion();
        Card second = new TrustyCompanion();
        Card unrevealed = new ToolcraftExemplar();
        harness.setLibrary(player1, List.of(first, second, unrevealed));
        harness.addMana(player1, ManaColor.WHITE, 2);

        tapAndResolve(depala);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, unrevealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
