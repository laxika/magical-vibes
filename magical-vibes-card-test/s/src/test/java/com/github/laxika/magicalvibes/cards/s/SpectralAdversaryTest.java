package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Geistwave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.OminousRoost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralAdversary.class, GrizzlyBears.class, LeoninScimitar.class,
        SilverBolt.class, OminousRoost.class, Geistwave.class})
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

    @Test
    void payingWithNoOtherPermanentsStillAddsCountersOnReflexiveResolution() {
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        Permanent adversary = findAdversary();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).isEmpty();
    }

    @Test
    void mayPayButChooseNoTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SilverBolt());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findAdversary().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    void phasesOutAnEnchantmentAndItReturnsOnlyDuringItsControllersUntap() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OminousRoost());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(enchantment);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingAdversaryInResponseDoesNotPreventPhasingOutItsTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SilverBolt());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handlePermanentChosen(player1, artifact.getId());

        Permanent adversary = findAdversary();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player2, 0, adversary.getId());
        harness.assertInHand(player1, "Spectral Adversary");
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact);
    }

    @Test
    void allTargetsBecomingIllegalPreventsCountersAsWellAsPhasing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SilverBolt());
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findAdversary().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Silver Bolt");
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).isEmpty();
    }

    @Test
    void paymentCountCanAllowMoreThanOneHundredTargets() {
        List<Permanent> artifacts = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            artifacts.add(harness.addToBattlefieldAndReturn(player2, new SilverBolt()));
        }
        harness.setHand(player1, List.of(new SpectralAdversary()));
        harness.addMana(player1, ManaColor.BLUE, 102);
        harness.addMana(player1, ManaColor.COLORLESS, 102);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 101);
        for (Permanent artifact : artifacts) {
            harness.handlePermanentChosen(player1, artifact.getId());
        }
        harness.passBothPriorities();

        assertThat(findAdversary().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(101);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).containsExactlyInAnyOrderElementsOf(artifacts);
    }

    private Permanent findAdversary() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpectralAdversary)
                .findFirst()
                .orElseThrow();
    }
}
