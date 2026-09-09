package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaSculptTest extends BaseCardTest {

    @Test
    void countersKickedSpellAndAddsManaEqualToActualPaymentAtNextMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new FugitiveWizard());

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.amount()).isEqualTo(5);
        assertThat(reward.color()).isEqualTo(ManaColor.COLORLESS);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    void countersSpellButDoesNotScheduleManaWithoutAWizard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }
}
