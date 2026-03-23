package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuatliDinosaurKnightTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        HuatliDinosaurKnight card = new HuatliDinosaurKnight();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+2 ability has PutPlusOnePlusOneCounterOnTargetCreatureEffect(2) with up-to-one targeting")
    void plusTwoAbilityHasCorrectEffect() {
        HuatliDinosaurKnight card = new HuatliDinosaurKnight();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(2);
        assertThat(ability.getMinTargets()).isZero();
        assertThat(ability.getMaxTargets()).isEqualTo(1);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        assertThat(((PutPlusOnePlusOneCounterOnTargetCreatureEffect) ability.getEffects().getFirst()).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 ability has FirstTargetDealsPowerDamageToSecondTargetEffect with multi-target")
    void minusThreeAbilityHasCorrectEffect() {
        HuatliDinosaurKnight card = new HuatliDinosaurKnight();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-3);
        assertThat(ability.isMultiTarget()).isTrue();
        assertThat(ability.getMultiTargetFilters()).hasSize(2);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(FirstTargetDealsPowerDamageToSecondTargetEffect.class);
    }

    @Test
    @DisplayName("-7 ability has BoostAllOwnCreaturesEffect(4, 4)")
    void minusSevenAbilityHasCorrectEffect() {
        HuatliDinosaurKnight card = new HuatliDinosaurKnight();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-7);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        BoostAllOwnCreaturesEffect effect = (BoostAllOwnCreaturesEffect) ability.getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(4);
        assertThat(effect.toughnessBoost()).isEqualTo(4);
    }

    // ===== Casting =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Huatli, Dinosaur Knight");
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
        Permanent huatli = bf.stream().filter(p -> p.getCard().getName().equals("Huatli, Dinosaur Knight")).findFirst().orElseThrow();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(4);
        assertThat(huatli.isSummoningSick()).isFalse();
    }

    // ===== +2 ability: Put two +1/+1 counters on up to one target Dinosaur you control =====

    @Test
    @DisplayName("+2 ability puts two +1/+1 counters on target Dinosaur and increases loyalty")
    void plusTwoPutsCountersOnDinosaur() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player1, new FrenziedRaptor());

        Permanent raptor = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 0, null, raptor.getId());
        harness.passBothPriorities();

        assertThat(huatli.getLoyaltyCounters()).isEqualTo(6); // 4 + 2
        assertThat(raptor.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("+2 ability can be activated without a target (up to zero)")
    void plusTwoCanActivateWithoutTarget() {
        Permanent huatli = addReadyHuatli(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getLoyaltyCounters()).isEqualTo(6); // 4 + 2
    }

    @Test
    @DisplayName("+2 ability cannot target a non-Dinosaur creature")
    void plusTwoCannotTargetNonDinosaur() {
        addReadyHuatli(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 ability cannot target an opponent's Dinosaur")
    void plusTwoCannotTargetOpponentDinosaur() {
        addReadyHuatli(player1);
        harness.addToBattlefield(player2, new FrenziedRaptor());

        Permanent raptor = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, raptor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== -3 ability: Target Dinosaur you control deals damage equal to its power to target creature you don't control =====

    @Test
    @DisplayName("-3 ability makes Dinosaur deal power damage to opponent's creature")
    void minusThreeDealsPowerDamage() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent raptor = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Frenzied Raptor is 4/2, Grizzly Bears is 2/2 — 4 damage kills it
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), bear.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(1); // 4 - 3
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-3 ability deals damage equal to Dinosaur's current power (with counters)")
    void minusThreeUsesCurrentPower() {
        Permanent huatli = addReadyHuatli(player1);
        harness.addToBattlefield(player1, new FrenziedRaptor());

        Permanent raptor = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        // Give raptor 2 extra +1/+1 counters -> 6/4
        raptor.setPlusOnePlusOneCounters(2);

        // Add a 5/5 creature for opponent
        harness.addToBattlefield(player2, new FrenziedRaptor());
        Permanent opponentDino = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        // Opponent's Raptor is 4/2, our Raptor deals 6 damage -> kills it
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), opponentDino.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Frenzied Raptor"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Frenzied Raptor"));
    }

    // ===== -7 ability: Dinosaurs you control get +4/+4 until end of turn =====

    @Test
    @DisplayName("-7 ability boosts all own Dinosaurs by +4/+4")
    void minusSevenBoostsDinosaurs() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setLoyaltyCounters(7);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Frenzied Raptor is 4/2 -> should be 8/6
        Permanent raptor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(6);

        // Grizzly Bears is not a Dinosaur -> should stay 2/2
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 ability does not boost opponent's Dinosaurs")
    void minusSevenDoesNotBoostOpponentDinosaurs() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setLoyaltyCounters(7);
        harness.addToBattlefield(player2, new FrenziedRaptor());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent opponentRaptor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, opponentRaptor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentRaptor)).isEqualTo(2);
    }

    // ===== Loyalty ability restrictions =====

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
        assertThat(huatli.getLoyaltyCounters()).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Planeswalker dies at 0 loyalty =====

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 but ability still resolves")
    void diesWhenLoyaltyReachesZeroAbilityStillResolves() {
        Permanent huatli = addReadyHuatli(player1);
        huatli.setLoyaltyCounters(3);
        harness.addToBattlefield(player1, new FrenziedRaptor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent raptor = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // -3 ability: 3 - 3 = 0, Huatli dies to state-based actions
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(raptor.getId(), bear.getId()));

        GameData gd = harness.getGameData();
        // Huatli should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Huatli, Dinosaur Knight"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Huatli, Dinosaur Knight"));

        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve - effects should still apply
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Grizzly Bears should be dead (4 damage from Frenzied Raptor)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyHuatli(Player player) {
        HuatliDinosaurKnight card = new HuatliDinosaurKnight();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
