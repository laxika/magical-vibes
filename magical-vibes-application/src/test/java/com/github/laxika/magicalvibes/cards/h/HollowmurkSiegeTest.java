package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowmurkSiege.class, Forest.class, GavonyTownship.class, GrizzlyBears.class})
class HollowmurkSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Sultai draws once when counters are put on your creatures")
    void sultaiDrawsOncePerTurn() {
        castAndChoose("Sultai");
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent firstCreature = addReadyCreature();
        Permanent secondCreature = addReadyCreature();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(township), 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Abzan puts a counter and gives menace to a target attacking creature")
    void abzanBuffsTargetAttacker() {
        castAndChoose("Abzan");
        Permanent attacker = addReadyCreature();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Sultai does not trigger the attack ability")
    void sultaiDoesNotTriggerAbzanAbility() {
        castAndChoose("Sultai");
        Permanent attacker = addReadyCreature();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new GrizzlyBears());
    }

    @Test
    @DisplayName("Sultai does not draw again for a separate counter event in the same turn")
    void sultaiLimitsSeparateCounterEvents() {
        castAndChoose("Sultai");
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent creature = addReadyCreature();
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateTownship(player1, firstTownship);
        activateTownship(player1, secondTownship);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Counters on opposing creatures do not consume Sultai's trigger")
    void opposingCountersDoNotConsumeSultaiTrigger() {
        castAndChoose("Sultai");
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent ownCreature = addReadyCreature();
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingTownship = harness.addToBattlefieldAndReturn(player2, new GavonyTownship());
        Permanent ownTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateTownship(player2, opposingTownship);

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        activateTownship(player1, ownTownship);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Abzan does not draw when a counter is put on your creature")
    void abzanDoesNotTriggerSultaiAbility() {
        castAndChoose("Abzan");
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent creature = addReadyCreature();
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateTownship(player1, township);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Abzan buffs only the selected attacker when multiple creatures attack")
    void abzanTriggersOnceForMultipleAttackers() {
        castAndChoose("Abzan");
        Permanent first = addReadyCreature();
        Permanent second = addReadyCreature();
        Permanent nonAttacker = addReadyCreature();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Sultai can draw again during the opponent's next turn")
    void sultaiResetsOnOpponentsTurn() {
        castAndChoose("Sultai");
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        Permanent creature = addReadyCreature();
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateTownship(player1, firstTownship);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondTownship),
                1, null, null);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Abzan's menace expires while its counter remains")
    void abzanMenaceExpiresAtEndOfTurn() {
        castAndChoose("Abzan");
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        Permanent attacker = addReadyCreature();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void activateTownship(Player player, Permanent township) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.activateAbility(player, gd.playerBattlefields.get(player.getId()).indexOf(township),
                1, null, null);
        resolveAllTriggers();
    }

    private Permanent castAndChoose(String mode) {
        harness.setHand(player1, List.of(new HollowmurkSiege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Sultai", "Abzan");
        harness.handleListChoice(player1, mode);

        return findPermanent(player1, "Hollowmurk Siege");
    }
}
