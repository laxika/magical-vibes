package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VladimirAndGodfrey.class, FyndhornElves.class, GrizzlyBears.class, TurnToFrog.class})
class VladimirAndGodfreyTest extends BaseCardTest {

    @Test
    void returnsTappedAndPerpetuallyGetsPlusOnePlusOne() {
        Card vladimirAndGodfrey = new VladimirAndGodfrey();
        harness.setGraveyard(player1, List.of(vladimirAndGodfrey));
        harness.addToBattlefield(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vladimir and Godfrey");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutControllingAOneOneCreature() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a 1/1 creature");
    }

    @Test
    void perpetualBoostAppliesAfterSettingBasePowerAndToughness() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        harness.addToBattlefield(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vladimir and Godfrey");
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void perpetualBoostSurvivesDeathAndAccumulatesOnTheNextReturn() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        harness.addToBattlefield(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Vladimir and Godfrey").setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Vladimir and Godfrey");
        addMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vladimir and Godfrey");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    void losingTheOneOneAfterActivationDoesNotPreventReturn() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();
        harness.activateGraveyardAbility(player1, 0);

        elf.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vladimir and Godfrey");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    void opponentsOneOneDoesNotEnableActivation() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        harness.addToBattlefield(player2, new FyndhornElves());
        addMana();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a 1/1 creature");
    }

    @Test
    void printedOneOneWithAPlusOneCounterDoesNotEnableActivation() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new FyndhornElves());
        elf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addMana();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a 1/1 creature");
    }

    @Test
    void returnsOnlyTheActivatedCopyFromTheGraveyard() {
        Card activated = new VladimirAndGodfrey();
        Card other = new VladimirAndGodfrey();
        harness.setGraveyard(player1, List.of(activated, other));
        harness.addToBattlefield(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(findPermanent(player1, "Vladimir and Godfrey").getCard().getId())
                .isEqualTo(activated.getId());
        assertThat(countPermanents(player1, "Vladimir and Godfrey")).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
