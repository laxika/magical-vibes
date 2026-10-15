package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoroGoroDiscipleOfRyusei.class, BearerOfMemory.class, AncestralKatana.class, ShortCircuit.class})
class GoroGoroDiscipleOfRyuseiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures haste until end of turn")
    void givesYourCreaturesHaste() {
        Permanent goroGoro = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent bear = addCreatureReady(player1, new BearerOfMemory());
        Permanent opposingBear = addCreatureReady(player2, new BearerOfMemory());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goroGoro, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goroGoro, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creates a flying 5/5 Dragon Spirit with an attacking modified creature")
    void createsDragonSpiritToken() {
        addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent modifiedAttacker = addCreatureReady(player1, new BearerOfMemory());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon Spirit");
        assertThat(countPermanents(player1, "Dragon Spirit")).isEqualTo(1);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON, CardSubtype.SPIRIT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot create a Dragon Spirit without an attacking modified creature")
    void requiresAttackingModifiedCreature() {
        addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent modifiedBear = addCreatureReady(player1, new BearerOfMemory());
        modifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hasteAppliesToCreaturesPresentAtResolutionOnly() {
        addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateWithUnmodifiedAttacker() {
        Permanent goroGoro = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        goroGoro.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opposingModifiedAttackerDoesNotEnableActivation() {
        addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent attacker = addCreatureReady(player2, new BearerOfMemory());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void equipmentEnablesActivationRegardlessOfItsController(boolean opponentControlsEquipment) {
        Permanent attacker = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent equipment = harness.addToBattlefieldAndReturn(
                opponentControlsEquipment ? player2 : player1, new AncestralKatana());
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon Spirit")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void auraEnablesActivationOnlyWhenYouControlIt(boolean opponentControlsAura) {
        Permanent attacker = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        Permanent aura = harness.addToBattlefieldAndReturn(
                opponentControlsAura ? player2 : player1, new ShortCircuit());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        if (opponentControlsAura) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            assertThat(countPermanents(player1, "Dragon Spirit")).isEqualTo(1);
        }
    }

    @Test
    void dragonAbilityStillResolvesAfterTheModifiedAttackerLeaves() {
        Permanent goroGoro = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        goroGoro.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        goroGoro.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(goroGoro);
        gd.playerGraveyards.get(player1.getId()).add(goroGoro.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon Spirit")).isEqualTo(1);
    }

    @Test
    void anyCounterEnablesActivationAndTokenDoesNotEnterAttacking() {
        Permanent goroGoro = addCreatureReady(player1, new GoroGoroDiscipleOfRyusei());
        goroGoro.setCounterCount(CounterType.CHARGE, 1);
        goroGoro.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon Spirit");
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }
}
