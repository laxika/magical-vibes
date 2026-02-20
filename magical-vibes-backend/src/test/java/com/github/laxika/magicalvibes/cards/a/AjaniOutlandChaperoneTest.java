package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenWithColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjaniOutlandChaperoneTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct card properties from Scryfall")
    void hasCorrectProperties() {
        AjaniOutlandChaperone card = new AjaniOutlandChaperone();

        assertThat(card.getName()).isEqualTo("Ajani, Outland Chaperone");
        assertThat(card.getType()).isEqualTo(CardType.PLANESWALKER);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getLoyalty()).isEqualTo(3);
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability creates a Kithkin token effect")
    void plusOneAbilityHasCorrectEffect() {
        AjaniOutlandChaperone card = new AjaniOutlandChaperone();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateCreatureTokenWithColorsEffect.class);
    }

    @Test
    @DisplayName("-2 ability deals 4 damage to target tapped creature")
    void minusTwoAbilityHasCorrectEffect() {
        AjaniOutlandChaperone card = new AjaniOutlandChaperone();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-2);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsTappedPredicate(),
                "Target must be a tapped creature"
        ));
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("-8 ability is the ultimate effect")
    void minusEightAbilityHasCorrectEffect() {
        AjaniOutlandChaperone card = new AjaniOutlandChaperone();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-8);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AjaniUltimateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ajani, Outland Chaperone");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));
        Permanent ajani = bf.stream().filter(p -> p.getCard().getName().equals("Ajani, Outland Chaperone")).findFirst().orElseThrow();
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(3);
        assertThat(ajani.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: Create a Kithkin token =====

    @Test
    @DisplayName("+1 ability creates a 1/1 Kithkin token and increases loyalty")
    void plusOneCreatesTokenAndIncreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(4);
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Kithkin")
                && p.getCard().getPower() == 1
                && p.getCard().getToughness() == 1);
    }

    // ===== -2 ability: Deal 4 damage to target tapped creature =====

    @Test
    @DisplayName("-2 ability deals 4 damage to tapped creature and decreases loyalty")
    void minusTwoDealsDamageAndDecreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(ajani.getLoyaltyCounters()).isEqualTo(1);
        // Grizzly Bears is 2/2, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-2 ability cannot target untapped creature")
    void minusTwoCannotTargetUntappedCreature() {
        addReadyAjani(player1);
        Permanent target = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
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
    @DisplayName("Cannot activate loyalty ability when stack is not empty")
    void cannotActivateWithNonEmptyStack() {
        addReadyAjani(player1);
        // Put something on the stack
        harness.getGameData().stack.add(new StackEntry(
                StackEntryType.CREATURE_SPELL,
                new GrizzlyBears(),
                player2.getId(),
                "Grizzly Bears",
                List.of()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
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
    @DisplayName("Cannot use -2 when loyalty is only 1")
    void cannotActivateNegativeCostWithInsufficientLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Planeswalker dies when loyalty reaches 0 =====

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 from ability activation")
    void diesWhenLoyaltyReachesZero() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(2);
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        // -2 ability: 2 - 2 = 0, Ajani dies to state-based actions after ability goes on stack
        harness.activateAbility(player1, 0, 1, null, target.getId());

        GameData gd = harness.getGameData();
        // Ajani should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));
        // Ajani goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ajani, Outland Chaperone"));
        // The ability is still on the stack though
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability still resolves after planeswalker dies to SBA at 0 loyalty")
    void abilityResolvesAfterPlaneswalkerDiesToSBA() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setLoyaltyCounters(2);
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        // -2 ability: loyalty 2 - 2 = 0, Ajani dies to SBA, ability stays on stack
        harness.activateAbility(player1, 0, 1, null, target.getId());

        // Ajani is already gone but the ability is on the stack
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));

        // Now resolve the ability — it should still deal 4 damage and kill Grizzly Bears
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Legend rule =====

    @Test
    @DisplayName("Legend rule applies to planeswalkers")
    void legendRuleApplies() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        // Cast a second one
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        // Legend rule: player should be prompted to choose which to keep
        // or one should be automatically removed. Check that there's only one Ajani on battlefield
        // (the exact handling depends on the legend rule implementation)
        GameData gd = harness.getGameData();
        long ajaniCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"))
                .count();
        // Either legend rule triggers a choice (interaction.awaitingInputType() != null) or only one remains
        assertThat(ajaniCount <= 1 || gd.interaction.awaitingInputType() != null).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyAjani(Player player) {
        AjaniOutlandChaperone card = new AjaniOutlandChaperone();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}



