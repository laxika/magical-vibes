package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GhostLitRedeemer.class)
class GhostLitRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: gains 2 life")
    void battlefieldAbilityGainsTwoLife() {
        Permanent redeemer = addCreatureReady(player1, new GhostLitRedeemer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(redeemer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Channel gains 4 life and discards Ghost-Lit Redeemer")
    void channelGainsFourLife() {
        harness.setHand(player1, List.of(new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        harness.assertInGraveyard(player1, "Ghost-Lit Redeemer");
    }

    @Test
    void tapAndDiscardCostsArePaidBeforeLifeGainResolves() {
        Permanent redeemer = addCreatureReady(player1, new GhostLitRedeemer());
        harness.setHand(player1, List.of(new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        assertThat(redeemer.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore);

        harness.activateHandAbility(player1, 0, null);
        harness.assertNotInHand(player1, "Ghost-Lit Redeemer");
        harness.assertInGraveyard(player1, "Ghost-Lit Redeemer");
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 6);
    }

    @Test
    void summoningSicknessPreventsTapAbility() {
        Permanent redeemer = harness.addToBattlefieldAndReturn(player1, new GhostLitRedeemer());
        redeemer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(redeemer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void tappedRedeemerCannotActivateAgain() {
        Permanent redeemer = addCreatureReady(player1, new GhostLitRedeemer());
        redeemer.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCannotDiscardWithoutEnoughMana() {
        harness.setHand(player1, List.of(new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ghost-Lit Redeemer");
        harness.assertNotInGraveyard(player1, "Ghost-Lit Redeemer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCanBeActivatedDuringOpponentsTurnAndGainsLifeForItsController() {
        harness.setHand(player2, List.of(new GhostLitRedeemer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int controllerLife = gd.getLife(player2.getId());
        int opponentLife = gd.getLife(player1.getId());

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player2, controllerLife + 4);
        harness.assertLife(player1, opponentLife);
        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
    }
}
