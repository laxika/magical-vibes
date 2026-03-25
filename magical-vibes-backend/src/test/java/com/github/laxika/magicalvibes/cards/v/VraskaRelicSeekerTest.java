package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToSpecificValueEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VraskaRelicSeekerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities with correct costs")
    void hasThreeLoyaltyAbilities() {
        VraskaRelicSeeker card = new VraskaRelicSeeker();
        assertThat(card.getActivatedAbilities()).hasSize(3);

        var plus2 = card.getActivatedAbilities().get(0);
        assertThat(plus2.getLoyaltyCost()).isEqualTo(2);
        assertThat(plus2.isNeedsTarget()).isFalse();
        assertThat(plus2.getEffects()).hasSize(1);
        assertThat(plus2.getEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);

        var minus3 = card.getActivatedAbilities().get(1);
        assertThat(minus3.getLoyaltyCost()).isEqualTo(-3);
        assertThat(minus3.isNeedsTarget()).isTrue();
        assertThat(minus3.getEffects()).hasSize(2);
        assertThat(minus3.getEffects().get(0)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(minus3.getEffects().get(1)).isInstanceOf(CreateTokenEffect.class);

        var minus10 = card.getActivatedAbilities().get(2);
        assertThat(minus10.getLoyaltyCost()).isEqualTo(-10);
        assertThat(minus10.isNeedsTarget()).isTrue();
        assertThat(minus10.getEffects()).hasSize(1);
        assertThat(minus10.getEffects().getFirst()).isInstanceOf(SetTargetPlayerLifeToSpecificValueEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VraskaRelicSeeker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vraska, Relic Seeker");
    }

    @Test
    @DisplayName("Resolving puts Vraska on battlefield with loyalty 6")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new VraskaRelicSeeker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Vraska, Relic Seeker"));
        Permanent vraska = findVraska(player1);
        assertThat(vraska.getLoyaltyCounters()).isEqualTo(6);
        assertThat(vraska.isSummoningSick()).isFalse();
    }

    // ===== +2 ability: Create a 2/2 black Pirate creature token with menace =====

    @Test
    @DisplayName("+2 creates a 2/2 black Pirate token with menace and increases loyalty")
    void plusTwoCreatesToken() {
        Permanent vraska = addReadyVraska(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vraska.getLoyaltyCounters()).isEqualTo(8); // 6 + 2

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Pirate"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Pirate token not found"));

        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PIRATE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("+2 can be activated multiple turns in a row")
    void plusTwoCreatesMultipleTokens() {
        Permanent vraska = addReadyVraska(player1);

        // First activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(vraska.getLoyaltyCounters()).isEqualTo(8);

        // Simulate new turn
        vraska.setLoyaltyActivationsThisTurn(0);

        // Second activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(vraska.getLoyaltyCounters()).isEqualTo(10);

        long pirateCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Pirate"))
                .count();
        assertThat(pirateCount).isEqualTo(2);
    }

    // ===== −3 ability: Destroy target artifact, creature, or enchantment. Create a Treasure token. =====

    @Test
    @DisplayName("-3 destroys target creature and creates a Treasure token")
    void minusThreeDestroysCreatureAndCreatesTreasure() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(vraska.getLoyaltyCounters()).isEqualTo(3); // 6 - 3

        // Bear should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Treasure token created for Vraska's controller
        Permanent treasure = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Treasure token not found"));

        assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("-3 destroys target enchantment and creates a Treasure token")
    void minusThreeDestroysEnchantment() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player2, new Pacifism());

        Permanent enchantment = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(vraska.getLoyaltyCounters()).isEqualTo(3); // 6 - 3

        // Enchantment should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pacifism"));

        // Treasure token should exist
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 rejects targeting a land")
    void minusThreeRejectsLand() {
        addReadyVraska(player1);
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.Forest());

        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 can target own permanent")
    void minusThreeCanTargetOwnPermanent() {
        Permanent vraska = addReadyVraska(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, null, ownBear.getId());
        harness.passBothPriorities();

        assertThat(vraska.getLoyaltyCounters()).isEqualTo(3); // 6 - 3

        // Own bear should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Treasure token should still be created for controller
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treasure"))
                .count()).isEqualTo(1);
    }

    // ===== −10 ability: Target player's life total becomes 1 =====

    @Test
    @DisplayName("-10 sets target opponent's life total to 1")
    void minusTenSetsOpponentLifeToOne() {
        Permanent vraska = addReadyVraska(player1);
        vraska.setLoyaltyCounters(10); // Need at least 10 loyalty

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeBefore).isEqualTo(GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(vraska.getLoyaltyCounters()).isEqualTo(0); // 10 - 10

        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(1);

        // Vraska goes to graveyard at 0 loyalty
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vraska, Relic Seeker"));
    }

    @Test
    @DisplayName("-10 can target self")
    void minusTenCanTargetSelf() {
        Permanent vraska = addReadyVraska(player1);
        vraska.setLoyaltyCounters(10);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        int lifeAfter = gd.playerLifeTotals.get(player1.getId());
        assertThat(lifeAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("-10 cannot be activated without enough loyalty")
    void minusTenRequiresTenLoyalty() {
        Permanent vraska = addReadyVraska(player1);
        // Default loyalty is 6, not enough for -10

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyVraska(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyVraska(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyVraska(Player player) {
        VraskaRelicSeeker card = new VraskaRelicSeeker();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(6);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent findVraska(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vraska, Relic Seeker"))
                .findFirst().orElseThrow();
    }
}
