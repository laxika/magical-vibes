package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorcererClass.class, GrizzlyBears.class, Shock.class})
class SorcererClassTest extends BaseCardTest {

    @Test
    void entersAndDrawsTwoThenDiscardsTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new SorcererClass(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelTwoLetsCreaturesProduceManaForInstantSorcerySpells() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        levelUpToTwo(sorcererClass);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void levelTwoManaCanPayForTheNextClassLevel() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        levelUpToTwo(sorcererClass);

        harness.activateAbility(player1, battlefieldIndex(creature), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null);
        harness.passBothPriorities();

        assertThat(sorcererClass.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
    }

    @Test
    void levelThreeDealsIncreasingDamageForEachInstantOrSorceryCastThisTurn() {
        Permanent sorcererClass = harness.addToBattlefieldAndReturn(player1, new SorcererClass());
        sorcererClass.setSummoningSick(false);
        levelUpToThree(sorcererClass);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.passBothPriorities();
    }

    private void levelUpToTwo(Permanent sorcererClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent sorcererClass) {
        levelUpToTwo(sorcererClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(sorcererClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
