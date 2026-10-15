package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderousDebut.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ThunderousDebutTest extends BaseCardTest {

    @Test
    void withoutBargainPutsUpToTwoCreatureCardsIntoHand() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new HillGiant();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Shock();
        setLibrary(firstCreature, firstNoncreature, secondCreature, secondNoncreature);
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == firstCreature || permanent.getCard() == secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainedCastPutsUpToTwoCreatureCardsOntoTheBattlefield() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new HillGiant();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Shock();
        setLibrary(firstCreature, firstNoncreature, secondCreature, secondNoncreature);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(firstCreature, secondCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotBargainBySacrificingACreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        assertThatThrownBy(() ->
                        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mayChooseNoCreaturesEvenWhenCreaturesAreAvailable(boolean bargained) {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        setLibrary(creature, noncreature);
        castAndResolve(bargained);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, noncreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Thunderous Debut");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void looksAtOnlyTwentyCardsAndMayChooseJustOne(boolean bargained) {
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        Card beyondTwenty = new HillGiant();
        List<Card> library = new ArrayList<>(List.of(first, second, third));
        for (int i = 0; i < 17; i++) {
            library.add(new Shock());
        }
        library.add(beyondTwenty);
        harness.setLibrary(player1, library);
        castAndResolve(bargained);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        if (bargained) {
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        } else {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }
        library.remove(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void resolvesWithNoCreaturesInLibrary(boolean bargained) {
        Card noncreature = new Shock();
        setLibrary(noncreature);
        castAndResolve(bargained);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Thunderous Debut");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void resolvesWithAnEmptyLibrary(boolean bargained) {
        harness.setLibrary(player1, List.of());
        castAndResolve(bargained);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Thunderous Debut");
    }

    private void castAndResolve(boolean bargained) {
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();
        if (bargained) {
            Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
            harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        } else {
            harness.castSorcery(player1, 0, 0);
        }
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
