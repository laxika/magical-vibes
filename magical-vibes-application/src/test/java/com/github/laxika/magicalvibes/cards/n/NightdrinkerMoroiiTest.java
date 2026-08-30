package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NightdrinkerMoroii.class)
class NightdrinkerMoroiiTest extends BaseCardTest {

    @Test
    @DisplayName("When cast face up, Nightdrinker Moroii makes its controller lose 3 life")
    void faceUpCastMakesControllerLoseLife() {
        harness.setHand(player1, List.of(new NightdrinkerMoroii()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Disguise enters face down and does not trigger the face-up ETB ability")
    void disguiseDoesNotTriggerEtbLifeLoss() {
        harness.setHand(player1, List.of(new NightdrinkerMoroii()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent moroii = findPermanent(player1, "Nightdrinker Moroii");
        assertThat(moroii.isFaceDown()).isTrue();
        harness.assertLife(player1, 20);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(moroii));
        harness.passBothPriorities();

        assertThat(moroii.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
    }
}
