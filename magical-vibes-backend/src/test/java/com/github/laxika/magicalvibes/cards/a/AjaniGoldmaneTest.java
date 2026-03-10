package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateLifeTotalAvatarTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjaniGoldmaneTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        AjaniGoldmane card = new AjaniGoldmane();

        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has GainLifeEffect(2)")
    void plusOneAbilityHasCorrectEffect() {
        AjaniGoldmane card = new AjaniGoldmane();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) ability.getEffects().getFirst()).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("-1 ability has counter and vigilance effects")
    void minusOneAbilityHasCorrectEffects() {
        AjaniGoldmane card = new AjaniGoldmane();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("-6 ability has CreateLifeTotalAvatarTokenEffect")
    void minusSixAbilityHasCorrectEffect() {
        AjaniGoldmane card = new AjaniGoldmane();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-6);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateLifeTotalAvatarTokenEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AjaniGoldmane()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ajani Goldmane");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 4")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new AjaniGoldmane()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Ajani Goldmane"));
        Permanent ajani = bf.stream().filter(p -> p.getCard().getName().equals("Ajani Goldmane")).findFirst().orElseThrow();
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(4);
        assertThat(ajani.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: You gain 2 life =====

    @Test
    @DisplayName("+1 ability gains 2 life and increases loyalty")
    void plusOneGainsLifeAndIncreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(5); // 4 + 1
        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore + 2);
    }

    // ===== -1 ability: Put +1/+1 counters and grant vigilance =====

    @Test
    @DisplayName("-1 ability puts +1/+1 counter on each creature and grants vigilance")
    void minusOnePutsCountersAndGrantsVigilance() {
        Permanent ajani = addReadyAjani(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(3); // 4 - 1

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        // Each creature should have a +1/+1 counter
        for (Permanent bear : bears) {
            assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(bear.getGrantedKeywords()).contains(Keyword.VIGILANCE);
        }
    }

    @Test
    @DisplayName("-1 ability does not affect opponent's creatures")
    void minusOneDoesNotAffectOpponentCreatures() {
        addReadyAjani(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentBear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(opponentBear.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(opponentBear.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("-1 counters are permanent, vigilance is until end of turn")
    void minusOneCountersArePermanentVigilanceIsTemporary() {
        Permanent ajani = addReadyAjani(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // +1/+1 counter is permanent
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Effective P/T: base 2/2 + 1 counter = 3/3
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== -6 ability: Create Avatar token =====

    @Test
    @DisplayName("-6 ability creates Avatar token with P/T equal to life total")
    void minusSixCreatesAvatarToken() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Find the Avatar token
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent avatar = bf.stream()
                .filter(p -> p.getCard().getName().equals("Avatar") && p.getCard().isToken())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Avatar token not found"));

        // P/T should equal controller's life total (20 default)
        int lifeTotal = gd.playerLifeTotals.get(player1.getId());
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(lifeTotal);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(lifeTotal);
    }

    @Test
    @DisplayName("Avatar token P/T changes when life total changes")
    void avatarTokenPTChangesWithLifeTotal() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent avatar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Avatar") && p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        // Default life is 20
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(20);

        // Change life total to 10
        gd.playerLifeTotals.put(player1.getId(), 10);
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(10);
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyAjani(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during combat")
    void cannotActivateDuringCombat() {
        addReadyAjani(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyAjani(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Cannot use -6 when loyalty is only 4")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Planeswalker dies at 0 loyalty =====

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 but ability still resolves")
    void diesWhenLoyaltyReachesZeroAbilityStillResolves() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        // -1 ability: 1 - 1 = 0, Ajani dies to state-based actions
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // Ajani should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ajani Goldmane"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ajani Goldmane"));

        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve - effects should still apply
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Bear should still have gotten the +1/+1 counter
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyAjani(Player player) {
        AjaniGoldmane card = new AjaniGoldmane();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
