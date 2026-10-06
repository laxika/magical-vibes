package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.cards.s.SummaryJudgment;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResoluteWatchdog.class, AxebaneBeast.class, GrotesqueDemise.class, SummaryJudgment.class})
class ResoluteWatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Pays one mana and sacrifices itself to grant a creature you control indestructible")
    void sacrificesItselfAndProtectsControlledCreature() {
        Permanent watchdog = addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(watchdog);
        harness.assertInGraveyard(player1, "Resolute Watchdog");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        addWatchdogReady(player1);
        Permanent target = addCreatureReady(player2, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent watchdog = harness.addToBattlefieldAndReturn(player1, new ResoluteWatchdog());
        watchdog.setSummoningSick(true);
        watchdog.setTapped(true);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Resolute Watchdog");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Resolute Watchdog");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotActivateWithoutManaAndDoesNotSacrifice() {
        Permanent watchdog = addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(watchdog, target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defenderCannotAttack() {
        addWatchdogReady(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfButIsSacrificedBeforeResolution() {
        Permanent watchdog = addWatchdogReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, watchdog.getId());

        harness.assertNotOnBattlefield(player1, "Resolute Watchdog");
        harness.assertInGraveyard(player1, "Resolute Watchdog");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Resolute Watchdog");
    }

    @Test
    void protectsAgainstLethalDamageInResponse() {
        addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        target.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SummaryJudgment()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Axebane Beast");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructibleDoesNotPreventExile() {
        addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Axebane Beast");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void abilityDoesNotProtectATargetExiledInResponse() {
        addWatchdogReady(player1);
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Resolute Watchdog");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addWatchdogReady(Player player) {
        return addCreatureReady(player, new ResoluteWatchdog());
    }
}
