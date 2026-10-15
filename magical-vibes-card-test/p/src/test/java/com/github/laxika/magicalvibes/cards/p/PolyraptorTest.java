package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Polyraptor.class, Shock.class, Bombard.class})
class PolyraptorTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, creates a 5/5 token copy")
    void damageCreatesTokenCopy() {
        harness.addToBattlefield(player2, new Polyraptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID polyraptorId = harness.getPermanentId(player2, "Polyraptor");
        harness.castAndResolveInstant(player1, 0, polyraptorId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Polyraptor")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Polyraptor"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(5);
                    assertThat(token.getCard().getToughness()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("Token copies retain the damage trigger")
    void tokenCopyRetainsDamageTrigger() {
        harness.addToBattlefield(player2, new Polyraptor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID polyraptorId = harness.getPermanentId(player2, "Polyraptor");
        harness.castAndResolveInstant(player1, 0, polyraptorId);
        harness.passBothPriorities();

        UUID tokenId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Polyraptor"))
                .findFirst()
                .orElseThrow()
                .getId();
        harness.castAndResolveInstant(player1, 0, tokenId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Polyraptor")).isEqualTo(3);
    }

    @Test
    @DisplayName("Each separate damage event creates a copy, including lethal damage")
    void lethalDamageStillCreatesCopy() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new Polyraptor());
        harness.setHand(player1, List.of(new Bombard(), new Bombard()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, source.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Polyraptor")).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertInGraveyard(player2, "Polyraptor");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(source.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Polyraptor")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getMarkedDamage()).isZero();
                });
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Copies do not inherit counters or marked damage")
    void copyDoesNotInheritCountersOrDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new Polyraptor());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(4);
        assertThat(source.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getPlusOnePlusOneCounters()).isZero();
                    assertThat(token.getMarkedDamage()).isZero();
                    assertThat(token.getCard().getPower()).isEqualTo(source.getCard().getPower());
                    assertThat(token.getCard().getToughness()).isEqualTo(source.getCard().getToughness());
                });
    }

    @Test
    @DisplayName("Lethal combat damage creates a copy for each controller")
    void lethalCombatDamageCreatesCopies() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Polyraptor());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Polyraptor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertInGraveyard(player1, "Polyraptor");
        harness.assertInGraveyard(player2, "Polyraptor");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Simultaneous damage from two blockers triggers only once")
    void simultaneousCombatDamageTriggersOnce() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Polyraptor());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new Polyraptor());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new Polyraptor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 5, secondBlocker.getId(), 0));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Polyraptor");
        assertThat(findPermanents(player1, "Polyraptor")).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(countPermanents(player2, "Polyraptor")).isEqualTo(2);
    }
}
