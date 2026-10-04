package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mirrorweave;
import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.cards.w.WastelandViper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Giant Adephage")
@CardUsed({GiantAdephage.class, Mirrorweave.class,
        RuinationWurm.class, WastelandViper.class})
class GiantAdephageTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates a token copy of this creature")
    void combatDamageCreatesTokenCopy() {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        adephage.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Adephage"))
                .hasSize(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p != adephage)
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(7);
        assertThat(token.getCard().getToughness()).isEqualTo(7);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("No token when the creature is blocked and deals no damage to the player")
    void noTokenWhenBlocked() {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        adephage.setAttacking(true);
        harness.setLife(player2, 20);

        // The 7/7 blocker receives all 7 damage, leaving none to trample over.
        Permanent blocker = addCreatureReady(player2, new GiantAdephage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Adephage"))
                .isEmpty();
        harness.assertInGraveyard(player1, "Giant Adephage");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Trample damage creates a copy even when deathtouch kills the source")
    void trampleDamageCreatesCopyAfterSourceDies() {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        adephage.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WastelandViper());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Giant Adephage");
        assertThat(countPermanents(player1, "Giant Adephage")).isZero();

        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Giant Adephage");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(countPermanents(player1, "Giant Adephage")).isEqualTo(1);
    }

    @Test
    @DisplayName("The copy does not inherit counters or combat status")
    void copyDoesNotInheritCountersOrCombatStatus() {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        adephage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        adephage.tap();
        adephage.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.assertLife(player2, 11);
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Giant Adephage").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The token copy retains the combat damage trigger")
    void tokenCopyCanCreateAnotherCopy() {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        adephage.setAttacking(true);
        harness.setLife(player2, 40);
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Giant Adephage").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        adephage.setAttacking(false);
        token.setSummoningSick(false);
        token.setAttacking(true);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 26);
        assertThat(countPermanents(player1, "Giant Adephage")).isEqualTo(3);
        assertThat(findPermanents(player1, "Giant Adephage"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("The trigger copies the source's current or last known copiable values")
    void copiesChangedSourceAtResolution(boolean sourceLeavesBattlefield) {
        Permanent adephage = addCreatureReady(player1, new GiantAdephage());
        Permanent wurm = addCreatureReady(player1, new RuinationWurm());
        adephage.setAttacking(true);
        harness.setHand(player1, List.of(new Mirrorweave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        resolveCombat();
        harness.castAndResolveInstant(player1, 0, wurm.getId());
        assertThat(adephage.getCard().getName()).isEqualTo("Ruination Wurm");
        if (sourceLeavesBattlefield) {
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, adephage));
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Ruination Wurm");
                    assertThat(token.getCard().getToughness()).isEqualTo(6);
                    assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.TRAMPLE);
                });
    }
}
