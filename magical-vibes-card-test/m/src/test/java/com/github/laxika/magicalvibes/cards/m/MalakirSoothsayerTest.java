package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.cards.k.KozileksTranslator;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalakirSoothsayer.class, HadaFreeblade.class, Forest.class, KozileksTranslator.class})
class MalakirSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally, draws a card, and loses 1 life")
    void cohortDrawsCardAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null);
        harness.passBothPriorities();

        assertThat(soothsayer.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(soothsayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Ally can pay the additional tap cost")
    void summoningSickAllyCanPayCohortCost() {
        MalakirSoothsayer drawnCard = new MalakirSoothsayer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new MalakirSoothsayer());

        harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null);

        assertThat(soothsayer.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A summoning-sick Soothsayer cannot activate cohort")
    void summoningSickSoothsayerCannotActivate() {
        Permanent soothsayer = harness.addToBattlefieldAndReturn(player1, new MalakirSoothsayer());
        Permanent ally = addCreatureReady(player1, new MalakirSoothsayer());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(soothsayer.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Ally cannot pay the additional tap cost")
    void tappedAllyCannotPayCohortCost() {
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent ally = addCreatureReady(player1, new MalakirSoothsayer());
        ally.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(soothsayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Ally cannot pay the additional tap cost")
    void opponentsAllyCannotPayCohortCost() {
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent ally = addCreatureReady(player2, new MalakirSoothsayer());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(soothsayer.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Soothsayer cannot activate cohort")
    void tappedSoothsayerCannotActivate() {
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent ally = addCreatureReady(player1, new MalakirSoothsayer());
        soothsayer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped non-Ally creature cannot pay the additional tap cost")
    void nonAllyCannotPayCohortCost() {
        Permanent soothsayer = addCreatureReady(player1, new MalakirSoothsayer());
        Permanent nonAlly = addCreatureReady(player1, new KozileksTranslator());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(soothsayer), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(soothsayer.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
