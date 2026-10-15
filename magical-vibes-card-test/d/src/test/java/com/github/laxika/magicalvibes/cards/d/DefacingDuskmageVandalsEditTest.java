package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VandalsEdit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefacingDuskmageVandalsEdit.class, VandalsEdit.class})
class DefacingDuskmageVandalsEditTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when an opponent draws their second card")
    void becomesPreparedOnOpponentsSecondDraw() {
        Permanent duskmage = addDuskmage();
        harness.setLibrary(player2, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));

        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(duskmage.isPrepared()).isTrue();
        UUID preparedSpellId = duskmage.getPreparedSpellCardId();
        assertThat(preparedSpellId).isNotNull();
        assertThat(gd.findExiledCard(preparedSpellId)).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(preparedSpellId, player1.getId());
    }

    @Test
    @DisplayName("Casting the prepared spell draws two cards and makes each player lose 2 life")
    void castingPreparedSpellDrawsAndLosesLife() {
        Permanent duskmage = addDuskmage();
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));
        harness.setLibrary(player2, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));

        draw(player2);
        draw(player2);
        harness.passBothPriorities();

        UUID preparedSpellId = duskmage.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, preparedSpellId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(duskmage.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(preparedSpellId)).isNull();
    }

    @Test
    @DisplayName("The controller's second draw does not prepare the creature")
    void controllersSecondDrawDoesNotPrepare() {
        Permanent duskmage = addDuskmage();
        harness.setLibrary(player1, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));

        draw(player1);
        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(duskmage.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("The opponent's third draw does not trigger another preparation")
    void opponentsThirdDrawDoesNotTrigger() {
        Permanent duskmage = addDuskmage();
        harness.setLibrary(player2, List.of(new DefacingDuskmageVandalsEdit(),
                new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));

        draw(player2);
        draw(player2);
        harness.passBothPriorities();
        UUID preparedSpellId = duskmage.getPreparedSpellCardId();

        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(duskmage.isPrepared()).isTrue();
        assertThat(duskmage.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
    }

    @Test
    @DisplayName("The first draw counts even when the creature enters afterward")
    void firstDrawBeforeEnteringStillCounts() {
        harness.setLibrary(player2, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));
        draw(player2);
        Permanent duskmage = addDuskmage();

        draw(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(duskmage.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("Casting a prepared spell unprepares immediately and creates no graveyard card")
    void unpreparesDuringCastingAndCopyDoesNotEnterGraveyard() {
        Permanent duskmage = addDuskmage();
        harness.setLibrary(player2, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));
        harness.setLibrary(player1, List.of(new DefacingDuskmageVandalsEdit(), new DefacingDuskmageVandalsEdit()));
        draw(player2);
        draw(player2);
        harness.passBothPriorities();
        UUID preparedSpellId = duskmage.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, preparedSpellId);

        assertThat(duskmage.isPrepared()).isFalse();
        assertThat(duskmage.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(preparedSpellId)).isNull();
    }

    private Permanent addDuskmage() {
        return harness.addToBattlefieldAndReturn(player1, new DefacingDuskmageVandalsEdit());
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
