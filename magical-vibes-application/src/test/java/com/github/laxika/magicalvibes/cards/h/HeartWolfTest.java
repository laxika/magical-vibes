package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AnHavvaTownship;
import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartWolf.class, DwarvenTrader.class, Roterothopter.class, AnHavvaTownship.class})
class HeartWolfTest extends BaseCardTest {

    private Permanent addWolfReady() {
        return addCreatureReady(player1, new HeartWolf());
    }

    private Permanent addDwarf() {
        return harness.addToBattlefieldAndReturn(player1, new DwarvenTrader());
    }

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Pumps target Dwarf and grants first strike during combat")
    void pumpsDwarfDuringCombat() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        int basePower = gqs.getEffectivePower(gd, dwarf);
        int baseToughness = gqs.getEffectiveToughness(gd, dwarf);

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Dwarf creature")
    void cannotTargetNonDwarf() {
        Permanent wolf = addWolfReady();
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Roterothopter());

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(wolf), 0, null, thopter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent wolf = addWolfReady();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AnHavvaTownship());

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(wolf), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent's Dwarf creature")
    void canTargetOpponentsDwarf() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addCreatureReady(player2, new DwarvenTrader());

        int basePower = gqs.getEffectivePower(gd, dwarf);
        int baseToughness = gqs.getEffectiveToughness(gd, dwarf);

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself when the boosted Dwarf leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, dwarf));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Heart Wolf");
        harness.assertInGraveyard(player1, "Heart Wolf");
    }

    @Test
    @DisplayName("Stays on the battlefield while the boosted Dwarf remains")
    void doesNotSacrificeIfTargetStays() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heart Wolf");
        harness.assertOnBattlefield(player1, "Dwarven Trader");
    }

    @Test
    @DisplayName("Pump and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        int basePower = gqs.getEffectivePower(gd, dwarf);
        int baseToughness = gqs.getEffectiveToughness(gd, dwarf);

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        Permanent after = gqs.findPermanentById(gd, dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Delayed sacrifice expires when the turn ends")
    void delayedSacrificeExpiresAtEndOfTurn() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        advanceToNextTurn();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, dwarf));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heart Wolf");
        harness.assertInHand(player1, "Dwarven Trader");
    }

    private void advanceToNextTurn() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
