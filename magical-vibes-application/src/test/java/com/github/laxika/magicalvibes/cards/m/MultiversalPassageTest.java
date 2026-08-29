package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MultiversalPassage.class)
class MultiversalPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a type and paying life makes Multiversal Passage an untapped land of that type")
    void choosingTypeAndPayingLife() {
        playPassage(20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ISLAND");

        Permanent passage = findPassage(player1);
        assertThat(passage.getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(passage.isTapped()).isFalse();
        assertThat(gqs.effectiveBasicLandTypes(gd, passage)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Declining to pay life makes Multiversal Passage enter tapped")
    void decliningLifePaymentEntersTapped() {
        playPassage(20);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "MOUNTAIN");

        Permanent passage = findPassage(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(passage.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, passage)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Multiversal Passage enters tapped when its controller cannot pay life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playPassage(1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "FOREST");

        Permanent passage = findPassage(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(passage.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, passage)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Multiversal Passage produces mana of its chosen type")
    void producesChosenMana() {
        Permanent passage = addPassageReady(player1, CardSubtype.SWAMP);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(passage.isTapped()).isTrue();
    }

    private void playPassage(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new MultiversalPassage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addPassageReady(Player player, CardSubtype chosenSubtype) {
        Permanent passage = new Permanent(new MultiversalPassage());
        passage.setChosenSubtype(chosenSubtype);
        passage.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(passage);
        return passage;
    }

    private Permanent findPassage(Player player) {
        return findPermanent(player, "Multiversal Passage");
    }
}
