package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BloodlinePretender.class, FugitiveWizard.class, GrizzlyBears.class})
class BloodlinePretenderTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as it enters sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new BloodlinePretender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        assertThat(findPermanent(player1, "Bloodline Pretender").getChosenSubtype())
                .isEqualTo(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("A creature you control of the chosen type puts a counter on Bloodline Pretender")
    void chosenTypeCreaturePutsCounterOnPretender() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature of a different type does not trigger Bloodline Pretender")
    void otherTypeDoesNotTrigger() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A matching creature entering under an opponent's control does not trigger Bloodline Pretender")
    void opponentCreatureDoesNotTrigger() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No creature type choice means no creature-enter trigger")
    void noCounterWithoutChoice() {
        Permanent pretender = new Permanent(new BloodlinePretender());
        gd.playerBattlefields.get(player1.getId()).add(pretender);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addPretender(Player player, CardSubtype chosen) {
        Permanent permanent = new Permanent(new BloodlinePretender());
        permanent.setChosenSubtype(chosen);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
