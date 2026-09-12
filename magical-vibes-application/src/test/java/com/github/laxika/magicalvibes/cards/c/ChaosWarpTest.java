package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosWarp.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class ChaosWarpTest extends BaseCardTest {

    @Test
    void shufflesTargetPermanentThenPutsPermanentTopCardOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void leavesRevealedNonPermanentCardInLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).contains(shock);
        harness.assertNotInGraveyard(player2, "Shock");
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
