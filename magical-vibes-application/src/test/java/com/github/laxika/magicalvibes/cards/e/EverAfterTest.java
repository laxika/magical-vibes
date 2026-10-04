package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverAfter.class, DevilthornFox.class, QuilledWolf.class})
class EverAfterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creatures as black Zombies")
    void returnsUpToTwoCreaturesAsBlackZombies() {
        Card fox = new DevilthornFox();
        Card wolf = new QuilledWolf();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(fox, wolf));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(fox.getId(), wolf.getId());
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId(), wolf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(fox.getId()) || p.getCard().getId().equals(wolf.getId()))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getGrantedColors()).contains(CardColor.BLACK);
                    assertThat(permanent.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
                });
    }

    @Test
    @DisplayName("Puts itself on the bottom of its owner's library")
    void putsItselfOnBottomOfOwnersLibrary() {
        Card fox = new DevilthornFox();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(fox));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(fox.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getGrantedColors()).contains(CardColor.BLACK);
                    assertThat(permanent.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(everAfter);
        assertThat(gd.playerDecks.get(player1.getId())).endsWith(everAfter);
    }

    @Test
    void canChooseZeroTargetsEvenWithCreatureCardsAvailable() {
        Card fox = new DevilthornFox();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(fox));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fox);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).endsWith(everAfter);
    }

    @Test
    void canResolveWithAnEmptyGraveyard() {
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).endsWith(everAfter);
    }

    @Test
    void onlyOwnCreatureCardsCanBeSelected() {
        Card fox = new DevilthornFox();
        Card opposingWolf = new QuilledWolf();
        Card noncreature = new EverAfter();
        harness.setGraveyard(player1, List.of(fox, noncreature));
        harness.setGraveyard(player2, List.of(opposingWolf));
        harness.setHand(player1, List.of(new EverAfter()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(fox.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingWolf.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingWolf);
    }

    @Test
    void returnsRemainingLegalTargetWhenOtherTargetLeavesGraveyard() {
        Card fox = new DevilthornFox();
        Card wolf = new QuilledWolf();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(fox, wolf));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId(), wolf.getId()));

        harness.setGraveyard(player1, List.of(wolf));
        harness.setExile(player1, List.of(fox));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(wolf);
            assertThat(permanent.getEffectiveColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
            assertThat(permanent.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.WOLF)).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.ZOMBIE)).isTrue();
            assertThat(permanent.isTapped()).isFalse();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).endsWith(everAfter);
    }

    @Test
    void goesToGraveyardInsteadOfLibraryWhenAllTargetsBecomeIllegal() {
        Card fox = new DevilthornFox();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(fox));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(fox));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(everAfter);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(everAfter);
    }

    @Test
    void cannotSelectMoreThanTwoCardsOrTheSameCardTwice() {
        Card fox = new DevilthornFox();
        Card wolf = new QuilledWolf();
        Card anotherWolf = new QuilledWolf();
        harness.setGraveyard(player1, List.of(fox, wolf, anotherWolf));
        harness.setHand(player1, List.of(new EverAfter()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(fox.getId(), wolf.getId(), anotherWolf.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(fox.getId(), fox.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(fox.getId(), wolf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(anotherWolf);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
