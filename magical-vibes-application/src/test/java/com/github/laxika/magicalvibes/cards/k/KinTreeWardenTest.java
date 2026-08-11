package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinTreeWardenTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityGrantsShield() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new KinTreeWarden());
        warden.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warden.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForGreen() {
        harness.setHand(player1, List.of(new KinTreeWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent warden = findPermanent(player1, "Kin-Tree Warden");
        assertThat(warden.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warden));
        harness.passBothPriorities();

        assertThat(warden.isFaceDown()).isFalse();
    }
}
