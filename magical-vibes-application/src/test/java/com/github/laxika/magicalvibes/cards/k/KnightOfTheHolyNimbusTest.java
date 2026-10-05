package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.StranglingSoot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheHolyNimbus.class, StranglingSoot.class, AshcoatBear.class})
class KnightOfTheHolyNimbusTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1")
    void flankingHitsNonFlankingBlocker() {
        addCreatureReady(player1, new KnightOfTheHolyNimbus());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker with flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        addCreatureReady(player1, new KnightOfTheHolyNimbus());
        Permanent blocker = addCreatureReady(player2, new KnightOfTheHolyNimbus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Intrinsic regeneration saves Knight of the Holy Nimbus from destruction")
    void intrinsicRegenerationSavesFromDestruction() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.setHand(player2, List.of(new StranglingSoot()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addStranglingSootMana();

        harness.castAndResolveInstant(player2, 0, knight.getId());

        harness.assertOnBattlefield(player1, "Knight of the Holy Nimbus");
        harness.assertNotInGraveyard(player1, "Knight of the Holy Nimbus");
        assertThat(knight.isTapped()).isTrue();
        assertThat(knight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only an opponent can activate the regeneration-prevention ability")
    void onlyOpponentCanActivateAbility() {
        addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents may activate this ability");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight of the Holy Nimbus");
        assertThat(knight.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Regeneration prevention expires at the end of the turn")
    void abilityPreventionExpiresAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.isCantRegenerateThisTurn()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.isCantRegenerateThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The opponent's ability prevents intrinsic regeneration this turn")
    void abilityPreventsIntrinsicRegeneration() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new StranglingSoot()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addStranglingSootMana();
        harness.castAndResolveInstant(player2, 0, knight.getId());

        harness.assertNotOnBattlefield(player1, "Knight of the Holy Nimbus");
        harness.assertInGraveyard(player1, "Knight of the Holy Nimbus");
    }

    @Test
    @DisplayName("Intrinsic regeneration can replace destruction repeatedly in the same turn")
    void regeneratesFromRepeatedDestruction() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        harness.setHand(player2, List.of(new StranglingSoot(), new StranglingSoot()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        addStranglingSootMana();
        harness.castAndResolveInstant(player2, 0, knight.getId());
        addStranglingSootMana();
        harness.castAndResolveInstant(player2, 0, knight.getId());

        harness.assertOnBattlefield(player1, "Knight of the Holy Nimbus");
        harness.assertNotInGraveyard(player1, "Knight of the Holy Nimbus");
        assertThat(knight.isTapped()).isTrue();
        assertThat(knight.getTimesRegeneratedThisTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal combat damage regenerates both Knights and removes them from combat")
    void regeneratesFromLethalCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        Permanent blocker = addCreatureReady(player2, new KnightOfTheHolyNimbus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Knight of the Holy Nimbus");
        harness.assertOnBattlefield(player2, "Knight of the Holy Nimbus");
        assertThat(attacker.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getTimesRegeneratedThisTurn()).isEqualTo(1);
        assertThat(blocker.getTimesRegeneratedThisTurn()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent can disable a tapped, summoning-sick Knight without affecting another Knight")
    void activationOnlyAffectsItsSourceAndRequiresNoTap() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfTheHolyNimbus());
        Permanent otherKnight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        knight.tap();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.isCantRegenerateThisTurn()).isTrue();
        assertThat(otherKnight.isCantRegenerateThisTurn()).isFalse();
    }
    private void addStranglingSootMana() {
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
