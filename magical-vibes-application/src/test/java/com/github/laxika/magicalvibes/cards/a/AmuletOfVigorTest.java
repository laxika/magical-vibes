package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JestersMask;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmuletOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a permanent you control that enters tapped")
    void untapsOwnPermanentThatEntersTapped() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.setHand(player1, List.of(new JestersMask()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Jester's Mask");
        assertThat(mask.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap an opponent's permanent")
    void doesNotUntapOpponentsPermanent() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new JestersMask()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent mask = findPermanent(player2, "Jester's Mask");
        assertThat(mask.isTapped()).isTrue();
    }
}
