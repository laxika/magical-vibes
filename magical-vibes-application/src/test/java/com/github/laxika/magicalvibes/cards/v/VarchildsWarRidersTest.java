package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VarchildsWarRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep gives the opponent a Survivor token and keeps the War-Riders")
    void payingUpkeepGivesOpponentASurvivor() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(riders.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
        assertThat(survivorCount(player2)).isEqualTo(1);
        assertThat(survivorCount(player1)).isZero();
    }

    @Test
    @DisplayName("Survivor tokens fall back to another set's printing when ALL has none")
    void survivorTokenFallsBackWhenSourceSetLacksPrinting() {
        CardPrintingRegistry.registerTokenImages("FALLBACK_SURV", Map.of(
                CardPrintingRegistry.buildTokenKey("Survivor", 1, 1, CardColor.RED),
                new CardPrintingRegistry.TokenImageData("tfallback", "1")));

        VarchildsWarRiders riders = new VarchildsWarRiders();
        riders.setSetCode("ALL");
        riders.setCollectorNumber("83");
        harness.addToBattlefieldAndReturn(player1, riders);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent survivor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> "Survivor".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(survivor.getCard().getSetCode()).isEqualTo("tfallback");
        assertThat(survivor.getCard().getCollectorNumber()).isEqualTo("1");
    }

    @Test
    @DisplayName("A second age counter makes the opponent create a second Survivor token")
    void secondUpkeepCreatesTwoMoreSurvivors() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(riders.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(riders);
        assertThat(survivorCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the War-Riders and creates no token")
    void decliningSacrifices() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new VarchildsWarRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riders);
        harness.assertInGraveyard(player1, "Varchild's War-Riders");
        assertThat(survivorCount(player2)).isZero();
    }

    @Test
    @DisplayName("With two blockers Rampage 1 grants +1/+1 until end of turn")
    void twoBlockersGivesPlusOne() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isEqualTo(1);
        assertThat(riders.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A single blocker grants no Rampage bonus")
    void oneBlockerGivesNothing() {
        Permanent riders = addReadyRiders(player1);
        riders.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(riders.getPowerModifier()).isZero();
        assertThat(riders.getToughnessModifier()).isZero();
    }

    private long survivorCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Survivor".equals(p.getCard().getName()))
                .count();
    }

    private Permanent addReadyRiders(Player player) {
        Permanent permanent = new Permanent(new VarchildsWarRiders());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
