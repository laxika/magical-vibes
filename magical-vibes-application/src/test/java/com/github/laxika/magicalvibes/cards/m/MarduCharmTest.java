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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduCharm.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        HillGiant.class, Peek.class})
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

    @Test
    @DisplayName("Mode 0 can damage a creature controlled by the caster")
    void damagesOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        cast(0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Mode 2 resolves without a choice when the opponent's hand is empty")
    void emptyHandDoesNotRequireChoice() {
        harness.setHand(player2, List.of());
        cast(2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mardu Charm");
    }

    @Test
    @DisplayName("Mode 2 discards nothing when the opponent holds only creatures and lands")
    void noEligibleCardDoesNotRequireChoice() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        cast(2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mardu Charm");
    }

    @Test
    @DisplayName("Mode 2 lets the caster choose one artifact from multiple eligible cards")
    void choosesExactlyOneEligibleCard() {
        harness.setHand(player2, List.of(new Peek(), new FountainOfYouth(), new GrizzlyBears()));
        cast(2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInHand(player2, "Peek");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MarduCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castModalInstant(player1, 0, modeIndex,
                targetId == null ? List.of() : List.of(targetId));
    }
}
