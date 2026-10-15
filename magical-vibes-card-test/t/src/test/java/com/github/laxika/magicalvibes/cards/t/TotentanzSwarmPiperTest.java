package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RagingBattleMouse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TotentanzSwarmPiper.class, RagingBattleMouse.class})
class TotentanzSwarmPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonblocking Rat when another nontoken creature you control dies")
    void createsRatWhenAnotherNontokenCreatureDies() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent mouse = addCreatureReady(player1, new RagingBattleMouse());

        destroyToGraveyard(mouse);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Creates a Rat when Totentanz dies")
    void createsRatWhenTotentanzDies() {
        Permanent totentanz = addCreatureReady(player1, new TotentanzSwarmPiper());

        destroyToGraveyard(totentanz);

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a Rat token dies")
    void doesNotTriggerForRatToken() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent mouse = addCreatureReady(player1, new RagingBattleMouse());
        destroyToGraveyard(mouse);

        Permanent rat = findPermanent(player1, "Rat");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rat));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("Gives an attacking Rat deathtouch until end of turn")
    void givesAttackingRatDeathtouchUntilEndOfTurn() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent mouse = addCreatureReady(player1, new RagingBattleMouse());
        destroyToGraveyard(mouse);

        Permanent rat = findPermanent(player1, "Rat");
        rat.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, rat.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-Rat attacking target")
    void rejectsNonRatTarget() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent attacker = addCreatureReady(player1, new RagingBattleMouse());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create a Rat for an opponent's nontoken creature")
    void doesNotTriggerForOpponentCreature() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent mouse = addCreatureReady(player2, new RagingBattleMouse());

        destroyToGraveyard(mouse);

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a Rat that is not attacking")
    void rejectsNonattackingRat() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        destroyToGraveyard(addCreatureReady(player1, new RagingBattleMouse()));
        Permanent rat = findPermanent(player1, "Rat");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rat.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects an opponent's attacking Rat")
    void rejectsOpponentRat() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        addCreatureReady(player2, new TotentanzSwarmPiper());
        destroyToGraveyard(addCreatureReady(player2, new RagingBattleMouse()));
        Permanent rat = findPermanent(player2, "Rat");
        rat.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rat.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant deathtouch if the Rat stops attacking before resolution")
    void rechecksAttackingTargetAtResolution() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        destroyToGraveyard(addCreatureReady(player1, new RagingBattleMouse()));
        Permanent rat = findPermanent(player1, "Rat");
        rat.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, rat.getId());

        rat.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Creates a Rat for each nontoken creature dying simultaneously, including Totentanz")
    void triggersForSimultaneousDeathsIncludingItself() {
        Permanent totentanz = addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent mouse = addCreatureReady(player1, new RagingBattleMouse());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .performSimultaneousRemovals(gd, java.util.List.of(totentanz, mouse), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, totentanz);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mouse);
                }));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }

    private void destroyToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
