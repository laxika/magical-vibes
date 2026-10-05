package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.s.SurviveTheNight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NotForgotten.class, DrownyardExplorers.class, SurviveTheNight.class})
class NotForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a targeted opponent graveyard card on top when chosen and creates a Spirit")
    void putsOpponentCardOnTopAndCreatesSpirit() {
        Card target = new DrownyardExplorers();
        Card oldTop = new SurviveTheNight();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(oldTop));
        castNotForgotten(target);

        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target, oldTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertSpiritCreated();
    }

    @Test
    @DisplayName("Puts a targeted card on the bottom when chosen")
    void putsCardOnBottom() {
        Card target = new SurviveTheNight();
        Card oldTop = new DrownyardExplorers();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        castNotForgotten(target);

        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(oldTop, target);
        assertSpiritCreated();
    }

    @Test
    @DisplayName("Fizzles without creating a Spirit if the targeted card leaves the graveyard")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new SurviveTheNight();
        harness.setGraveyard(player1, List.of(target));
        prepareNotForgotten();
        harness.castSorcery(player1, 0, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof NotForgotten);
    }

    @Test
    @DisplayName("Creates the Spirit only after the graveyard card is moved")
    void waitsForDestinationChoiceBeforeCreatingSpirit() {
        Card target = new DrownyardExplorers();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        castNotForgotten(target);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertSpiritCreated();
    }

    @Test
    @DisplayName("Can put a noncreature card on the bottom of an opponent's empty library")
    void putsOpponentNoncreatureOnBottomOfEmptyLibrary() {
        Card target = new SurviveTheNight();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        castNotForgotten(target);

        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertSpiritCreated();
    }

    private void castNotForgotten(Card target) {
        prepareNotForgotten();
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }

    private void prepareNotForgotten() {
        harness.setHand(player1, List.of(new NotForgotten()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void assertSpiritCreated() {
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactly(CardColor.WHITE);
    }
}
