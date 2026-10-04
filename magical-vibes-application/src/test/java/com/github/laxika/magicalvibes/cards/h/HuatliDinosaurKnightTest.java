package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuatliDinosaurKnight.class, FrenziedRaptor.class, DeeprootWarrior.class, LoomingAltisaur.class})
class HuatliDinosaurKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HuatliDinosaurKnight()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(HuatliDinosaurKnight.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 4")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new HuatliDinosaurKnight()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Huatli, Dinosaur Knight"));
        Permanent huatli = findPermanent(player1, "Huatli, Dinosaur Knight");
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(huatli.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+2 ability puts two +1/+1 counters on target Dinosaur and increases loyalty")
    void plusTwoPutsCountersOnDinosaur() {
        Permanent huatli = addReadyHuatli(player1);
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());

        harness.activateAbility(player1, 0, 0, null, raptor.getId());
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 4 + 2
        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("+2 ability can be activated without a target (up to zero)")
    void plusTwoCanActivateWithoutTarget() {
        Permanent huatli = addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 4 + 2
    }

    @Test
    @DisplayName("+2 ability cannot target a non-Dinosaur creature")
    void plusTwoCannotTargetNonDinosaur() {
        addReadyHuatli(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 ability cannot target an opponent's Dinosaur")
    void plusTwoCannotTargetOpponentDinosaur() {
        addReadyHuatli(player1);
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new FrenziedRaptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, raptor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 ability makes Dinosaur deal power damage to opponent's creature")
    void minusThreeDealsPowerDamage() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player2, new DeeprootWarrior());

        Permanent raptor = findPermanent(player1, "Frenzied Raptor");

        Permanent bear = findPermanent(player2, "Deeproot Warrior");

        // Frenzied Raptor is 4/2, Deeproot Warrior is 2/2 — 4 damage kills it
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3
        harness.assertNotOnBattlefield(player2, "Deeproot Warrior");
        harness.assertInGraveyard(player2, "Deeproot Warrior");
    }

    @Test
    @DisplayName("-3 ability deals damage equal to Dinosaur's current power (with counters)")
    void minusThreeUsesCurrentPower() {
        addReadyHuatli(player1);
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        // Give raptor 2 extra +1/+1 counters -> 6/4
        raptor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        Permanent opponentDino = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), opponentDino.getId()));
        harness.passBothPriorities();

        assertThat(opponentDino.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Looming Altisaur");
    }

    @Test
    @DisplayName("-7 ability boosts all own Dinosaurs by +4/+4")
    void minusSevenBoostsDinosaurs() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player1, new DeeprootWarrior());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Frenzied Raptor is 4/2 -> should be 8/6
        Permanent raptor = findPermanent(player1, "Frenzied Raptor");
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(6);

        // Deeproot Warrior is not a Dinosaur -> should stay 2/2
        Permanent bear = findPermanent(player1, "Deeproot Warrior");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 ability does not boost opponent's Dinosaurs")
    void minusSevenDoesNotBoostOpponentDinosaurs() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new FrenziedRaptor());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent opponentRaptor = findPermanent(player2, "Frenzied Raptor");
        assertThat(gqs.getEffectivePower(gd, opponentRaptor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentRaptor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyHuatli(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during combat")
    void cannotActivateDuringCombat() {
        addReadyHuatli(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Cannot use -7 when loyalty is only 4")
    void cannotActivateMinusSevenWithInsufficientLoyalty() {
        Permanent huatli = addReadyHuatli(player1);
        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 but ability still resolves")
    void diesWhenLoyaltyReachesZeroAbilityStillResolves() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player2, new DeeprootWarrior());

        Permanent raptor = findPermanent(player1, "Frenzied Raptor");
        Permanent bear = findPermanent(player2, "Deeproot Warrior");

        // -3 ability: 3 - 3 = 0, Huatli dies to state-based actions
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), bear.getId()));

        GameData gd = harness.getGameData();
        // Huatli should be in graveyard
        harness.assertNotOnBattlefield(player1, "Huatli, Dinosaur Knight");
        harness.assertInGraveyard(player1, "Huatli, Dinosaur Knight");

        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve - effects should still apply
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Deeproot Warrior should be dead (4 damage from Frenzied Raptor)
        harness.assertNotOnBattlefield(player2, "Deeproot Warrior");
        harness.assertInGraveyard(player2, "Deeproot Warrior");
    }

    @Test
    @DisplayName("-3 does not deal damage when the Dinosaur changes controller before resolution")
    void minusThreeDoesNothingWhenDinosaurBecomesIllegal() {
        addReadyHuatli(player1);
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), victim.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(raptor);
        gd.playerBattlefields.get(player2.getId()).add(raptor);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("-3 cannot use a non-Dinosaur as the damage source")
    void minusThreeRejectsNonDinosaurSource() {
        addReadyHuatli(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 cannot target a creature you control as the damage recipient")
    void minusThreeRejectsOwnVictim() {
        addReadyHuatli(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new LoomingAltisaur());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 uses power at resolution and does not make the victim deal damage back")
    void minusThreeUsesPowerAtResolutionWithoutRetaliation() {
        addReadyHuatli(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), victim.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(6);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("-7 affects multiple Dinosaurs present at resolution and wears off at end of turn")
    void minusSevenLocksInCreaturesAndExpires() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setCounterCount(CounterType.LOYALTY, 8);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LoomingAltisaur());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(11);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 keeps its loyalty increase when its target changes controller before resolution")
    void plusTwoDoesNotPutCountersOnIllegalTarget() {
        Permanent huatli = addReadyHuatli(player1);
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        harness.activateAbility(player1, 0, 0, null, raptor.getId());
        gd.playerBattlefields.get(player1.getId()).remove(raptor);
        gd.playerBattlefields.get(player2.getId()).add(raptor);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("-3 does not deal damage when the victim becomes controlled by you")
    void minusThreeDoesNothingWhenVictimBecomesIllegal() {
        addReadyHuatli(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FrenziedRaptor());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerBattlefields.get(player1.getId()).add(victim);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
    }

    private Permanent addReadyHuatli(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HuatliDinosaurKnight());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
