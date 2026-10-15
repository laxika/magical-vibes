package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckDefinition;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.DeckValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersistentPetitioners.class, SenateCourier.class})
class PersistentPetitionersTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability mills one card and taps Persistent Petitioners")
    void firstAbilityMillsOneCard() {
        Permanent petitioners = addReadyPetitioners();
        Card topCard = new SenateCourier();
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(petitioners.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Tapping four Advisors mills twelve cards")
    void fourAdvisorsMillTwelveCards() {
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        List<Permanent> advisors = new ArrayList<>(List.of(petitioners));
        for (int i = 0; i < 3; i++) {
            advisors.add(harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners()));
        }

        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new SenateCourier());
        }
        harness.setLibrary(player2, library);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(petitioners);

        harness.activateAbility(player1, sourceIndex, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(12);
        assertThat(advisors).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("The twelve-card ability cannot be activated without four Advisors")
    void cannotActivateWithoutFourAdvisors() {
        harness.addToBattlefield(player1, new PersistentPetitioners());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new SenateCourier());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstAbilityCannotBeActivatedWithSummoningSickness() {
        harness.addToBattlefield(player1, new PersistentPetitioners());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstAbilityRequiresOneMana() {
        Permanent petitioners = addReadyPetitioners();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(petitioners.isTapped()).isFalse();
    }

    @Test
    void firstAbilityCanTargetControllerAndMillsOnlyTopCard() {
        addReadyPetitioners();
        Card topCard = new SenateCourier();
        Card nextCard = new SenateCourier();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void secondAbilityCanUseOtherAdvisorsWhileSourceIsTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        source.tap();
        List<Permanent> advisors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            advisors.add(harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners()));
        }
        Card remainingCard = new SenateCourier();
        harness.setLibrary(player1, List.of(remainingCard));

        harness.activateAbility(player1, 0, 1, null, player1.getId());

        assertThat(advisors).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void tappedAdvisorCannotPaySecondAbilityCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new PersistentPetitioners());
        }
        harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsAdvisorsCannotPaySecondAbilityCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new PersistentPetitioners());
        }
        harness.addToBattlefield(player2, new PersistentPetitioners());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void advisorsTappedForOneAbilityCannotPayForAnotherActivation() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PersistentPetitioners());
        }
        harness.setLibrary(player2, List.of(new SenateCourier()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void secondAbilityTapsOnlyFourChosenAdvisorsAndMillsOnlyTwelveCards() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        List<Permanent> advisors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            advisors.add(harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners()));
        }
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            library.add(new SenateCourier());
        }
        harness.setLibrary(player2, library);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        for (Permanent advisor : advisors) {
            harness.handlePermanentChosen(player1, advisor.getId());
        }

        assertThat(source.isTapped()).isFalse();
        assertThat(advisors).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library.subList(0, 12));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(12));
    }

    @Test
    void deckMayContainMoreThanFourPetitioners() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new PersistentPetitioners());
        }

        assertThat(new DeckValidationService(null)
                .validate(new DeckDefinition(cards, List.of(), null), DeckFormat.CASUAL).valid()).isTrue();
    }

    private Permanent addReadyPetitioners() {
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        return petitioners;
    }
}
