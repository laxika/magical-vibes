package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.m.MadameVastra;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JennyFlint.class, Clue.class, Food.class, MadameVastra.class})
class JennyFlintTest extends BaseCardTest {

    @Test
    void sacrificingCluePutsCounterOnAnotherCreatureYouControl() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent bears = addCreatureReady(player1, new MadameVastra());
        Permanent clue = harness.addToBattlefieldAndReturn(player1, new Clue());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingFoodPutsCounterOnAnotherCreatureYouControl() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent bears = addCreatureReady(player1, new MadameVastra());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new Food());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void trainingPutsCounterOnJennyWhenSheAttacksWithALargerCreature() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        addCreatureReady(player1, new MadameVastra());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void firstStrikeKillsBlockerBeforeItCanDamageJenny() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent blocker = addCreatureReady(player2, new MadameVastra());
        blocker.setPowerModifier(-1);
        blocker.setToughnessModifier(-1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jenny);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    void trainingDoesNotTriggerWhenJennyAttacksAlone() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        addCreatureReady(player1, new MadameVastra());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void trainingDoesNotTriggerWithAnEqualPowerAttacker() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent vastra = addCreatureReady(player1, new MadameVastra());
        vastra.setPowerModifier(-1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sacrificingClueOnlyAllowsAnotherCreatureYouControlAsTarget() {
        addCreatureReady(player1, new JennyFlint());
        Permanent vastra = addCreatureReady(player1, new MadameVastra());
        addCreatureReady(player2, new MadameVastra());
        Permanent clue = harness.addToBattlefieldAndReturn(player1, new Clue());
        harness.addToBattlefield(player1, new Food());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(vastra.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, vastra.getId());
        resolveAllTriggers();

        assertThat(vastra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsClueSacrificeDoesNotTriggerJenny() {
        Permanent jenny = addCreatureReady(player1, new JennyFlint());
        Permanent vastra = addCreatureReady(player1, new MadameVastra());
        Permanent clue = harness.addToBattlefieldAndReturn(player2, new Clue());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vastra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card vastra = new MadameVastra();
        harness.setLibrary(player2, List.of(vastra));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new JennyFlint());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(vastra);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void partnerWithLetsTheTargetPlayerSearchForMadameVastra() {
        Card vastra = new MadameVastra();
        harness.setLibrary(player2, List.of(vastra));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new JennyFlint());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(vastra);
    }
}
