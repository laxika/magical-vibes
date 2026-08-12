package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarduCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 deals 4 damage to a target creature")
    void damagesTargetCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        cast(0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Mode 0 cannot target a noncreature permanent")
    void modeZeroRejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        assertThatThrownBy(() -> cast(0, harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Mode 1 creates two Warrior tokens with first strike until end of turn")
    void createsWarriorTokensWithFirstStrike() {
        cast(1, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isFalse());
    }

    @Test
    @DisplayName("Mode 2 lets you choose a noncreature, nonland card for an opponent to discard")
    void discardsChosenNoncreatureNonlandCard() {
        harness.setHand(player2, List.of(new Peek(), new Forest(), new GrizzlyBears()));
        cast(2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 2 can target only an opponent")
    void modeTwoRejectsSelfTarget() {
        assertThatThrownBy(() -> cast(2, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MarduCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, modeIndex, targetId,
                null, List.of(), List.of());
    }
}
