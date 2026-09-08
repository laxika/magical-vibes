package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecretPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Face-down creatures you control get +0/+1")
    void boostsYourFaceDownCreatures() {
        harness.addToBattlefield(player1, new SecretPlans());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AinokTracker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AinokTracker());
        ownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        opponentCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Turning a permanent you control face up draws a card")
    void drawsWhenYourPermanentTurnsFaceUp() {
        harness.addToBattlefield(player1, new SecretPlans());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new AinokTracker());
        tracker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
