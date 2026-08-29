package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhostLitRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: gains 2 life")
    void battlefieldAbilityGainsTwoLife() {
        Permanent redeemer = harness.addToBattlefieldAndReturn(player1, new GhostLitRedeemer());
        redeemer.setSummoningSick(false);
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
}
