package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkitteringSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("Skittering Surveyor has ETB may search for basic land effect")
    void hasCorrectEffects() {
        SkitteringSurveyor card = new SkitteringSurveyor();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SearchLibraryForBasicLandToHandEffect.class);
    }

    @Test
    @DisplayName("Resolving Skittering Surveyor creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve creature spell → creature enters

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skittering Surveyor"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability allows choosing a basic land from library")
    void acceptingMayAllowsChoosingBasicLand() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.librarySearch().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Declining may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("searches their library"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SkitteringSurveyor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
