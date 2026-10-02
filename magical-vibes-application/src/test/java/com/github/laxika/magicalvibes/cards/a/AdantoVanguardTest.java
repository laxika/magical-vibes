package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.u.UnfriendlyFire;
import com.github.laxika.magicalvibes.cards.w.WalkThePlank;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdantoVanguard.class, WalkThePlank.class, UnfriendlyFire.class})
class AdantoVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 while attacking")
    void getsPlusTwoPlusZeroWhileAttacking() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        // 1/1 + 2/0 = 3/1 while attacking
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost when not attacking")
    void noBoostWhenNotAttacking() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());

        // Not attacking — should remain 1/1
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying 4 life grants indestructible until end of turn")
    void payLifeGrantsIndestructible() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(vanguard.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot activate ability with less than 4 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Can activate multiple times to pay more life")
    void canActivateMultipleTimes() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(vanguard.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(vanguard.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vanguard.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Activation at exactly 4 life is accepted but player loses before ability resolves (CR 704.5a)")
    void canActivateAtExactlyFourLife() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 4);

        // Activation is accepted (4 >= 4), life cost is paid, but SBAs fire immediately
        // and the player loses at 0 life before the ability resolves (CR 704.3 / 704.5a)
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(vanguard.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void attackBonusEndsAfterCombatAndDoesNotBoostOtherCreatures() {
        Permanent attacking = addCreatureReady(player1, new AdantoVanguard());
        Permanent idle = addCreatureReady(player1, new AdantoVanguard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gqs.getEffectivePower(gd, attacking)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, idle)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 17);
        assertThat(gqs.getEffectivePower(gd, attacking)).isEqualTo(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndOnlyProtectsSource() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new AdantoVanguard());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AdantoVanguard());
        vanguard.setSummoningSick(true);
        vanguard.setTapped(true);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 16);
        assertThat(vanguard.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.passBothPriorities();

        assertThat(vanguard.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(vanguard.isTapped()).isTrue();
    }

    @Test
    void indestructiblePreventsDestroyEffect() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, vanguard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof AdantoVanguard);
    }

    @Test
    void indestructiblePreventsLethalDamageDestruction() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new UnfriendlyFire()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, vanguard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof AdantoVanguard);
    }
}
