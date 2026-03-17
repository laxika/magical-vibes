package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimoireOfTheDeadTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Grimoire of the Dead has two activated abilities with correct effects")
    void hasCorrectAbilities() {
        GrimoireOfTheDead card = new GrimoireOfTheDead();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 1: {1}, {T}, Discard a card: Put a study counter
        var ability0 = card.getActivatedAbilities().get(0);
        assertThat(ability0.isRequiresTap()).isTrue();
        assertThat(ability0.getManaCost()).isEqualTo("{1}");
        assertThat(ability0.getEffects()).hasSize(2);
        assertThat(ability0.getEffects().get(0)).isInstanceOf(DiscardCardTypeCost.class);
        assertThat(((DiscardCardTypeCost) ability0.getEffects().get(0)).requiredType()).isNull();
        assertThat(ability0.getEffects().get(1)).isInstanceOf(PutCounterOnSelfEffect.class);
        assertThat(((PutCounterOnSelfEffect) ability0.getEffects().get(1)).counterType()).isEqualTo(CounterType.STUDY);

        // Ability 2: {T}, Remove three study counters and sacrifice: Return all creatures
        var ability1 = card.getActivatedAbilities().get(1);
        assertThat(ability1.isRequiresTap()).isTrue();
        assertThat(ability1.getManaCost()).isNull();
        assertThat(ability1.getEffects()).hasSize(3);
        assertThat(ability1.getEffects().get(0)).isInstanceOf(RemoveCounterFromSourceCost.class);
        RemoveCounterFromSourceCost removeCost = (RemoveCounterFromSourceCost) ability1.getEffects().get(0);
        assertThat(removeCost.count()).isEqualTo(3);
        assertThat(removeCost.counterType()).isEqualTo(CounterType.STUDY);
        assertThat(ability1.getEffects().get(1)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability1.getEffects().get(2)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Ability 1: Put study counter =====

    @Test
    @DisplayName("Activating ability 1 starts discard-cost choice for any card")
    void ability1StartsDiscardChoice() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        // Any card should be valid for discard
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a card to discard puts ability on stack and adds study counter on resolution")
    void ability1AddsStudyCounterOnResolution() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Discard was paid
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Resolve the ability
        harness.passBothPriorities();

        assertThat(grimoire.getStudyCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 can accumulate multiple study counters")
    void ability1AccumulatesCounters() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(grimoire.getStudyCounters()).isEqualTo(1);

        // Untap for next activation
        grimoire.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(grimoire.getStudyCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability 1 without a card in hand")
    void ability1RequiresCardInHand() {
        addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    @DisplayName("Cannot activate ability 1 without enough mana")
    void ability1RequiresMana() {
        addReadyGrimoire();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 2: Sacrifice and return all creatures =====

    @Test
    @DisplayName("Ability 2 returns all creature cards from all graveyards to battlefield")
    void ability2ReturnsAllCreaturesFromAllGraveyards() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both creatures should be on player1's battlefield
        long creatureCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(creatureCount).isEqualTo(2);

        // Graveyards should be empty of creatures
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ability 2 sacrifices Grimoire as cost")
    void ability2SacrificesGrimoire() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // Grimoire should be gone from battlefield (sacrificed as cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grimoire of the Dead"));
    }

    @Test
    @DisplayName("Returned creatures gain Zombie subtype in addition to their other types")
    void returnedCreaturesGainZombieSubtype() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        // Original subtypes preserved
        assertThat(bears.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Returned creatures gain black color in addition to their other colors")
    void returnedCreaturesGainBlackColor() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(bears.getGrantedColors()).contains(CardColor.BLACK);
        // Original color preserved on card
        assertThat(bears.getCard().getColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate ability 2 without three study counters")
    void ability2RequiresThreeStudyCounters() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Study counters are removed as cost for ability 2")
    void ability2RemovesStudyCounters() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setStudyCounters(3);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // After activation the permanent is sacrificed, so counters are gone with it
        // But we can verify the ability goes on stack (meaning cost was paid)
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // ===== Helper =====

    private Permanent addReadyGrimoire() {
        GrimoireOfTheDead card = new GrimoireOfTheDead();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
