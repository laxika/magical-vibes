package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdantoVanguardTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct static effect: +2/+0 while attacking")
    void hasCorrectStaticEffect() {
        AdantoVanguard card = new AdantoVanguard();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
        assertThat(boost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);
    }

    @Test
    @DisplayName("Has correct activated ability: pay 4 life for indestructible")
    void hasCorrectActivatedAbility() {
        AdantoVanguard card = new AdantoVanguard();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(PayLifeCost.class);
        PayLifeCost lifeCost = (PayLifeCost) ability.getEffects().get(0);
        assertThat(lifeCost.amount()).isEqualTo(4);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) ability.getEffects().get(1);
        assertThat(grant.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Static boost: +2/+0 while attacking =====

    @Test
    @DisplayName("Gets +2/+0 while attacking")
    void getsPlusTwoPlusZeroWhileAttacking() {
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());

        declareAttackers(player1, List.of(0));

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

    // ===== Activated ability: pay 4 life for indestructible =====

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

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int idx : attackerIndices) {
            battlefield.get(idx).setAttacking(true);
        }
    }
}
