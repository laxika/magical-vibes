package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeverancePriest.class, Forest.class, GrizzlyBears.class, Murder.class})
class SeverancePriestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals the opponent's hand and allows choosing a nonland card")
    void etbExilesChosenNonlandCard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        castAndResolveEtb(List.of(creature, land));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("When it leaves, the exiled card's owner creates an X/X Spirit")
    void leavesCreatesTokenForExiledCardOwner() {
        Card exiled = new GrizzlyBears();
        castAndResolveEtb(List.of(exiled));
        harness.handleCardChosen(player1, 0);

        destroyPriest();

        Permanent spirit = findPermanent(player2, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("If no card is exiled, leaving creates no token")
    void noExiledCardMeansNoToken() {
        castAndResolveEtb(List.of(new Forest()));
        assertThat(gd.interaction.activeInteraction()).isNull();

        destroyPriest();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spirit"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("The ETB can target only an opponent")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new SeverancePriest()));
        addPriestMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castAndResolveEtb(List<Card> targetHand) {
        harness.setHand(player1, List.of(new SeverancePriest()));
        harness.setHand(player2, targetHand);
        addPriestMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyPriest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Severance Priest"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addPriestMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
