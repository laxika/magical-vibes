package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RecumbentBliss;
import com.github.laxika.magicalvibes.cards.p.PunctureBlast;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StillmoonCavalier.class, SpiritOfTheHearth.class, RecumbentBliss.class, SoulReap.class, PunctureBlast.class, SoulSnuffers.class})
class StillmoonCavalierTest extends BaseCardTest {

    private Permanent addCavalierReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StillmoonCavalier());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Flying ability grants flying, payable with white or black mana")
    void flyingAbilityGrantsFlying() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("First strike ability grants first strike")
    void firstStrikeAbilityGrantsFirstStrike() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Pump ability gives +1/+0 until end of turn")
    void pumpAbilityBoosts() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(cavalier.getEffectivePower()).isEqualTo(3);
        assertThat(cavalier.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(cavalier.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be targeted by a white Aura")
    void cannotBeTargetedByWhiteAura() {
        Permanent cavalier = addCavalierReady(player2);

        // Add a legal target so the spell is playable.
        harness.addToBattlefield(player2, new SpiritOfTheHearth());

        harness.setHand(player1, List.of(new RecumbentBliss()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, cavalier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Cannot be targeted by a black sorcery")
    void cannotBeTargetedByBlackSorcery() {
        Permanent cavalier = addCavalierReady(player2);

        harness.addToBattlefield(player2, new SpiritOfTheHearth());

        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, cavalier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent cavalier = addCavalierReady(player1);

        harness.setHand(player1, List.of(new PunctureBlast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, cavalier.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(PunctureBlast.class);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cavalier);
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    void pumpAcceptsEveryHybridPayment(int white, int black) {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, white);
        harness.addMana(player1, ManaColor.BLACK, black);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(cavalier.getEffectivePower()).isEqualTo(3);
        assertThat(cavalier.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void firstStrikePaidWithBlackExpiresAtCleanup() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void repeatedPumpActivationsAccumulate() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cavalier.getEffectivePower()).isEqualTo(4);
        assertThat(cavalier.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void abilitiesWorkWhileTappedAndSummoningSickWithoutTargetingSelf() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new StillmoonCavalier());
        cavalier.setSummoningSick(true);
        cavalier.tap();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(cavalier.getEffectivePower()).isEqualTo(3);
        assertThat(cavalier.isTapped()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void redManaCannotPayHybridAbilities(int abilityIndex) {
        addCavalierReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void whiteAndBlackCreaturesCannotBlockCavalier() {
        Permanent attacker = addCavalierReady(player1);
        Permanent whiteBlocker = harness.addToBattlefieldAndReturn(player2, new SpiritOfTheHearth());
        Permanent blackAndWhiteBlocker = addCavalierReady(player2);

        assertThat(bls.canBlockAttacker(gd, whiteBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackAndWhiteBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void whiteCombatDamageIsPrevented() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SpiritOfTheHearth());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        cavalier.setBlocking(true);
        cavalier.addBlockingTarget(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cavalier);
        assertThat(cavalier.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void blackCombatDamageIsPrevented() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SoulSnuffers());
        attacker.setSummoningSick(false);
        Permanent blocker = addCavalierReady(player2);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
