package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriestOfTheWakeningSunTest extends BaseCardTest {

    // ── Card structure ────────────────────────────────────────────────

    @Test
    @DisplayName("Has upkeep triggered MayRevealSubtypeFromHandEffect for Dinosaur")
    void hasUpkeepTrigger() {
        PriestOfTheWakeningSun card = new PriestOfTheWakeningSun();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayRevealSubtypeFromHandEffect.class);
        MayRevealSubtypeFromHandEffect effect =
                (MayRevealSubtypeFromHandEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(effect.thenEffect()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) effect.thenEffect()).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Has activated ability with sacrifice cost and Dinosaur search")
    void hasActivatedAbility() {
        PriestOfTheWakeningSun card = new PriestOfTheWakeningSun();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}{W}{W}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(SearchLibraryForCardTypesToHandEffect.class);
        SearchLibraryForCardTypesToHandEffect search =
                (SearchLibraryForCardTypesToHandEffect) ability.getEffects().get(1);
        assertThat(search.filter()).isInstanceOf(CardSubtypePredicate.class);
        assertThat(((CardSubtypePredicate) search.filter()).subtype()).isEqualTo(CardSubtype.DINOSAUR);
    }

    // ── Upkeep trigger ────────────────────────────────────────────────

    @Test
    @DisplayName("Upkeep with Dinosaur in hand — accept reveals and gains 2 life")
    void upkeepWithDinosaurGainsLife() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Upkeep with Dinosaur in hand — decline does not gain life")
    void upkeepWithDinosaurDeclined() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep without Dinosaur in hand — no prompt, no life gain")
    void upkeepWithoutDinosaurNoTrigger() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no Dinosaur → no trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // opponent's upkeep — no trigger for player1

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ── Activated ability: sacrifice + search ─────────────────────────

    @Test
    @DisplayName("Activated ability sacrifices Priest and searches for Dinosaur")
    void activatedAbilitySearchesDinosaur() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a Dinosaur in library
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrazingWhiptail(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Priest should be sacrificed (gone from battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Priest of the Wakening Sun"));

        // Library search should be awaiting input
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getSubtypes())
                .contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Choosing a Dinosaur from library puts it into hand")
    void choosingDinosaurPutsIntoHand() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrazingWhiptail(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the Dinosaur
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grazing Whiptail"));
    }

    @Test
    @DisplayName("Activated ability requires {3}{W}{W} mana")
    void activatedAbilityRequiresMana() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        // Only add 4 mana (need 5)
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Should not be able to activate — not enough mana
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Priest of the Wakening Sun"));

        // Priest stays on battlefield (ability not activated)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Priest goes to graveyard after sacrifice")
    void priestGoesToGraveyardAfterSacrifice() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new FrenziedRaptor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Priest of the Wakening Sun"));
    }
}
