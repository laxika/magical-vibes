package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoltaicVisionary.class, VoltChargedBerserker.class, GrizzlyBears.class, Mountain.class})
class VoltaicVisionaryTest extends BaseCardTest {

    @Test
    void damagesControllerAndTracksTopCardWithPlayPermission() {
        Card topCard = new GrizzlyBears();
        Permanent visionary = activateWithTopCard(topCard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(visionary.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    void transformsWhenCardExiledWithItIsCast() {
        Card topCard = new GrizzlyBears();
        Permanent visionary = activateWithTopCard(topCard);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(visionary.isTransformed()).isTrue();
        assertThat(visionary.getCard()).isInstanceOf(VoltChargedBerserker.class);
    }

    @Test
    void transformsWhenCardExiledWithItIsPlayedAsALand() {
        Card topCard = new Mountain();
        Permanent visionary = activateWithTopCard(topCard);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(visionary.isTransformed()).isTrue();
        assertThat(visionary.getCard()).isInstanceOf(VoltChargedBerserker.class);
    }

    @Test
    void activationIsSorcerySpeed() {
        addVisionary();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent activateWithTopCard(Card topCard) {
        Permanent visionary = addVisionary();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return visionary;
    }

    private Permanent addVisionary() {
        Permanent visionary = harness.addToBattlefieldAndReturn(player1, new VoltaicVisionary());
        visionary.setSummoningSick(false);
        return visionary;
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
