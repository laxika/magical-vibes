package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InkDissolver;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.l.LatchkeyFaerie;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StinkdrinkerBandit.class, InkDissolver.class, LatchkeyFaerie.class,
        AmoeboidChangeling.class, Lignify.class})
class StinkdrinkerBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked Rogue gets +2/+1 until end of turn")
    void unblockedRogueGetsBoost() {
        Permanent bandit = addCreatureReady(player1, new StinkdrinkerBandit());
        addCreatureReady(player2, new InkDissolver()); // a potential blocker that declines to block

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of()); // no blocks — the Bandit is unblocked
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(2);
        assertThat(bandit.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked Rogue does not get the boost")
    void blockedRogueGetsNoBoost() {
        Permanent bandit = addCreatureReady(player1, new StinkdrinkerBandit());
        addCreatureReady(player2, new InkDissolver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(0);
        assertThat(bandit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Another unblocked Rogue gets +2/+1")
    void anotherUnblockedRogueGetsBoost() {
        addCreatureReady(player1, new StinkdrinkerBandit());
        Permanent rogue = addCreatureReady(player1, new LatchkeyFaerie());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(rogue.getPowerModifier()).isEqualTo(2);
        assertThat(rogue.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Rogue attacking unblocked is not boosted")
    void nonRogueUnblockedNotBoosted() {
        addCreatureReady(player1, new StinkdrinkerBandit()); // source, not attacking
        Permanent nonRogue = addCreatureReady(player1, new InkDissolver());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(nonRogue.getPowerModifier()).isEqualTo(0);
        assertThat(nonRogue.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +2/+1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bandit = addCreatureReady(player1, new StinkdrinkerBandit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bandit.getPowerModifier()).isEqualTo(0);
        assertThat(bandit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be cast for its prowl cost after Goblin combat damage")
    void prowlAvailableAfterGoblinDamage() {
        recordCombatDamageSubtype(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new StinkdrinkerBandit()));
        harness.addMana(player1, ManaColor.BLACK, 2); // prowl {1}{B}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stinkdrinker Bandit");
    }

    @Test
    @DisplayName("Can be cast for its prowl cost after Rogue combat damage")
    void prowlAvailableAfterRogueDamage() {
        recordCombatDamageSubtype(CardSubtype.ROGUE);

        harness.setHand(player1, List.of(new StinkdrinkerBandit()));
        harness.addMana(player1, ManaColor.BLACK, 2); // prowl {1}{B}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stinkdrinker Bandit");
    }

    @Test
    @DisplayName("Prowl is unavailable without qualifying combat damage")
    void prowlUnavailableWithoutQualifyingDamage() {
        recordCombatDamageSubtype(CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new StinkdrinkerBandit()));
        harness.addMana(player1, ManaColor.BLACK, 2); // enough for prowl {1}{B}, not for the normal {3}{B}

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Bandits each boost every unblocked Rogue")
    void multipleBanditsBoostEachUnblockedRogue() {
        Permanent first = addCreatureReady(player1, new StinkdrinkerBandit());
        Permanent second = addCreatureReady(player1, new StinkdrinkerBandit());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(4);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(4);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Actual Rogue combat damage enables prowl in the second main phase")
    void rogueCombatDamageEnablesProwl() {
        addCreatureReady(player1, new LatchkeyFaerie());
        harness.setHand(player1, List.of(new StinkdrinkerBandit()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.assertLife(player2, 17);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stinkdrinker Bandit");
    }

    @Test
    @DisplayName("A creature that gains all creature types qualifies as a Rogue")
    void gainedRogueTypeQualifiesForBoost() {
        addCreatureReady(player1, new StinkdrinkerBandit());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent attacker = addCreatureReady(player1, new InkDissolver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(2));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Rogue that loses all creature types does not qualify for the boost")
    void lostRogueTypeDoesNotQualifyForBoost() {
        addCreatureReady(player1, new StinkdrinkerBandit());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent attacker = addCreatureReady(player1, new LatchkeyFaerie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 1, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(2));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Bandit that has lost its abilities does not boost an unblocked Rogue")
    void banditWithoutAbilitiesDoesNotTrigger() {
        Permanent bandit = addCreatureReady(player1, new StinkdrinkerBandit());
        Permanent rogue = addCreatureReady(player1, new LatchkeyFaerie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, bandit.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(rogue.getPowerModifier()).isZero();
        assertThat(rogue.getToughnessModifier()).isZero();
    }

    private void recordCombatDamageSubtype(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
    }
}
