package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SeparatePermanentsIntoPilesAndSacrificeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaOfTheVeilTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        LilianaOfTheVeil card = new LilianaOfTheVeil();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has EachPlayerDiscardsEffect(1)")
    void plusOneAbilityHasCorrectEffect() {
        LilianaOfTheVeil card = new LilianaOfTheVeil();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(EachPlayerDiscardsEffect.class);
        assertThat(((EachPlayerDiscardsEffect) ability.getEffects().getFirst()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 ability has SacrificeCreatureEffect targeting player")
    void minusTwoAbilityHasCorrectEffect() {
        LilianaOfTheVeil card = new LilianaOfTheVeil();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-2);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(SacrificeCreatureEffect.class);
    }

    @Test
    @DisplayName("-6 ability has SeparatePermanentsIntoPilesAndSacrificeEffect targeting player")
    void minusSixAbilityHasCorrectEffect() {
        LilianaOfTheVeil card = new LilianaOfTheVeil();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-6);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(SeparatePermanentsIntoPilesAndSacrificeEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LilianaOfTheVeil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Liliana of the Veil");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 3")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new LilianaOfTheVeil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Liliana of the Veil"));
        Permanent liliana = bf.stream().filter(p -> p.getCard().getName().equals("Liliana of the Veil")).findFirst().orElseThrow();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(3);
    }

    // ===== +1 ability: Each player discards a card =====

    @Test
    @DisplayName("+1 ability makes each player discard a card and increases loyalty")
    void plusOneEachPlayerDiscards() {
        Permanent liliana = addReadyLiliana(player1);

        // Give both players a card in hand
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(4); // 3 + 1

        // Active player (player1) discards first — enters discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    // ===== -2 ability: Target player sacrifices a creature =====

    @Test
    @DisplayName("-2 ability forces target player to sacrifice a creature")
    void minusTwoTargetPlayerSacrificesCreature() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(5);

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(3); // 5 - 2
        // With one creature, it's auto-sacrificed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-2 ability with multiple creatures prompts choice")
    void minusTwoWithMultipleCreaturesPromptsChoice() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(5);

        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
    }

    @Test
    @DisplayName("-2 ability has no effect when target has no creatures")
    void minusTwoNoEffectWhenNoCreatures() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(5);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(3); // 5 - 2
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    // ===== -6 ability: Separate permanents into two piles =====

    @Test
    @DisplayName("-6 ability prompts controller to separate permanents into two piles")
    void minusSixPromptsPileSeparation() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(6);

        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(0); // 6 - 6
        // Controller should be prompted to choose permanents for pile 1
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingPileSeparation).isTrue();
    }

    @Test
    @DisplayName("-6 ability: target player sacrifices chosen pile 1")
    void minusSixTargetPlayerSacrificesPile1() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(6);

        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Step 1: Controller (player1) assigns bears to pile 1, spider to pile 2
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        GameData gd = harness.getGameData();
        // Step 2: Target player (player2) should be prompted to choose a pile
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Target player chooses Yes = sacrifice pile 1 (bears)
        harness.handleMayAbilityChosen(player2, true);

        gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-6 ability: target player sacrifices chosen pile 2")
    void minusSixTargetPlayerSacrificesPile2() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(6);

        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Step 1: Controller assigns bears to pile 1, spider to pile 2
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Step 2: Target player chooses No = sacrifice pile 2 (spider)
        harness.handleMayAbilityChosen(player2, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("-6 ability: all permanents in one pile, empty other pile")
    void minusSixAllInOnePile() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(6);

        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Controller puts everything in pile 1 (pile 2 is empty)
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), spider.getId()));

        // Target player chooses No = sacrifice pile 2 (empty)
        harness.handleMayAbilityChosen(player2, false);

        GameData gd = harness.getGameData();
        // Both permanents should survive since pile 2 was empty
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("-6 ability: no effect when target has no permanents")
    void minusSixNoEffectWhenNoPermanents() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(6);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingPileSeparation).isFalse();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no permanents to separate"));
    }

    @Test
    @DisplayName("Cannot activate -6 when loyalty is only 3")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        addReadyLiliana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyLiliana(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Complete the discard interactions
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyLiliana(Player player) {
        LilianaOfTheVeil card = new LilianaOfTheVeil();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
