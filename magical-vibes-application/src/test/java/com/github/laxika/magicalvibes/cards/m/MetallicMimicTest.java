package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetallicMimic.class, FugitiveWizard.class, GrizzlyBears.class})
class MetallicMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as it enters sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new MetallicMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        assertThat(findPermanent(player1, "Metallic Mimic").getChosenSubtype())
                .isEqualTo(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Metallic Mimic is the chosen type in addition to its other types")
    void isTheChosenTypeItself() {
        Permanent mimic = addMimic(player1, CardSubtype.WIZARD);

        assertThat(gqs.computeStaticBonus(gd, mimic).grantedSubtypes()).contains(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Another creature you control of the chosen type enters with an additional +1/+1 counter")
    void chosenTypeCreatureEntersWithCounter() {
        addMimic(player1, CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A creature of a different type gets no counter")
    void otherTypeGetsNoCounter() {
        addMimic(player1, CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No counter is granted before a creature type has been chosen")
    void noCounterWithoutChoice() {
        harness.addToBattlefield(player1, new MetallicMimic());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's creature of the chosen type does not get a counter")
    void opponentCreatureGetsNoCounter() {
        addMimic(player1, CardSubtype.WIZARD);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two Metallic Mimics naming the same type grant two additional counters")
    void twoMimicsGrantTwoCounters() {
        addMimic(player1, CardSubtype.WIZARD);
        addMimic(player1, CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("A Mimic does not give itself a counter when choosing Shapeshifter")
    void doesNotGiveItselfCounter() {
        harness.setHand(player1, List.of(new MetallicMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SHAPESHIFTER");

        assertThat(findPermanent(player1, "Metallic Mimic").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("An entering Mimic receives a counter from an existing Mimic naming its chosen type")
    void enteringMimicReceivesCounterForChosenType() {
        Permanent first = addMimic(player1, CardSubtype.WIZARD);
        harness.setHand(player1, List.of(new MetallicMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        Permanent second = findPermanents(player1, "Metallic Mimic").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Being a Shapeshifter does not make Mimic grant counters to Shapeshifters when naming Wizard")
    void naturalSourceTypeDoesNotDetermineCounterRecipients() {
        Permanent first = addMimic(player1, CardSubtype.WIZARD);
        harness.setHand(player1, List.of(new MetallicMimic()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        Permanent second = findPermanents(player1, "Metallic Mimic").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addMimic(Player player, CardSubtype chosen) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MetallicMimic());
        perm.setChosenSubtype(chosen);
        return perm;
    }
}
