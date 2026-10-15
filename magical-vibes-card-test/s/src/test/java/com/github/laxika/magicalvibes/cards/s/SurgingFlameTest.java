package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgingFlame.class, FrostRaptor.class, JaceBeleren.class})
class SurgingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to any target")
    void dealsDamageToAnyTarget() {
        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new FrostRaptor());
        UUID targetId = harness.getPermanentId(player2, "Frost Raptor");

        castSurgingFlame(targetId);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frost Raptor");
    }

    @Test
    @DisplayName("Ripple offers revealed cards with the same name")
    void rippleOffersSameNameCards() {
        prepareCaster(List.of(
                new FrostRaptor(),
                new SurgingFlame(),
                new FrostRaptor(),
                new SurgingFlame()));

        castSurgingFlame(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.INSTANT_SPELL
                && entry.getCard().getName().equals("Surging Flame"));
    }

    @Test
    @DisplayName("Ripple can free-cast a revealed Surging Flame without paying mana")
    void rippleFreeCastsMatchingSpell() {
        prepareCaster(List.of(new SurgingFlame(), new FrostRaptor()));

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Declining ripple leaves the library unchanged")
    void decliningRippleLeavesLibraryUnchanged() {
        List<Card> library = List.of(new SurgingFlame(), new FrostRaptor());
        prepareCaster(library);

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Accepting ripple with an empty library still resolves the spell")
    void rippleWithEmptyLibrary() {
        prepareCaster(List.of());

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Surging Flame");
    }

    @Test
    @DisplayName("Ripple reveals only four cards and bottoms declined cards in chosen order")
    void rippleBottomsOnlyTopFourCards() {
        Card first = new FrostRaptor();
        Card second = new SurgingFlame();
        Card third = new FrostRaptor();
        Card fourth = new FrostRaptor();
        Card fifth = new SurgingFlame();
        prepareCaster(List.of(first, second, third, fourth, fifth));

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, fourth, third, second, first);
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({SurgingFlame.class, JaceBeleren.class})
    @DisplayName("A Surging Flame cast with ripple can target a planeswalker")
    void rippleFreeCastCanTargetPlaneswalker() {
        prepareCaster(List.of(new SurgingFlame()));
        var planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ripple's target choices exclude creatures with shroud")
    void rippleFreeCastCannotTargetShroudedCreature() {
        var raptor = harness.addToBattlefieldAndReturn(player2, new FrostRaptor());
        gd.playerManaPools.get(player2.getId()).addSnowMana(ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        prepareCaster(List.of(new SurgingFlame()));

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(raptor.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "Frost Raptor");
    }

    private void prepareCaster(List<Card> libraryTop) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, libraryTop);
    }

    private void castSurgingFlame(UUID targetId) {
        harness.setHand(player1, List.of(new SurgingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
