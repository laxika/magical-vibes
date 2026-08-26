package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralAdversary.class, GrizzlyBears.class, LeoninScimitar.class})
class SpectralAdversaryTest extends BaseCardTest {

    @Test
    void paysTwiceAndPhasesOutUpToTwoOtherPermanents() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 2);

        Permanent adversary = findAdversary();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownArtifact.getId(), opponentCreature.getId())
                .doesNotContain(adversary.getId());

        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(adversary);
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent adversary = findAdversary();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of())).isEmpty();
    }

    private Permanent findAdversary() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpectralAdversary)
                .findFirst()
                .orElseThrow();
    }
}
