package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FarsightMaskTest extends BaseCardTest {

    @Test
    void opponentDamageMayDraw() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedMaskDoesNotTrigger() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new FarsightMask());
        mask.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void ownDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
