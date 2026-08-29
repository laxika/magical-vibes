package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenGrotto.class, GrizzlyBears.class})
class HiddenGrottoTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveilOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HiddenGrotto()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent grotto = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(grotto.isTapped()).isFalse();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Surveil 1 can put the top card into the graveyard")
    void surveilPutsTopCardIntoGraveyard() {
        GameData gameData = harness.getGameData();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HiddenGrotto()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent grotto = addReadyGrotto();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {1} and tapping adds one mana of the chosen color")
    void payingOneAddsChosenColorMana() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyGrotto() {
        Permanent grotto = new Permanent(new HiddenGrotto());
        grotto.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(grotto);
        return grotto;
    }
}
