package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversionChamberTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        ConversionChamber card = new ConversionChamber();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability exiles target artifact from graveyard and puts charge counter on self")
    void firstAbilityStructure() {
        ConversionChamber card = new ConversionChamber();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.getEffects())
                .anyMatch(e -> e instanceof ExileTargetCardFromGraveyardEffect ex
                        && ex.requiredType() == CardType.ARTIFACT)
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);
    }

    @Test
    @DisplayName("Second ability removes 1 charge counter and creates a 3/3 Golem token")
    void secondAbilityStructure() {
        ConversionChamber card = new ConversionChamber();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.getEffects())
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof CreateCreatureTokenEffect);
    }

    // ===== First ability — exile artifact from graveyard and gain charge counter =====

    @Test
    @DisplayName("Activating first ability exiles artifact from controller's graveyard and adds charge counter")
    void firstAbilityExilesArtifactAndAddsCounter() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod removed from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));

        // Rod is in player's exiled cards
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));

        // Charge counter added
        assertThat(chamber.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability can exile artifact from opponent's graveyard")
    void firstAbilityExilesFromOpponentGraveyard() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod removed from opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));

        // Rod is in opponent's exiled cards (cards owned by graveyard owner)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));

        // Charge counter still added to chamber
        assertThat(chamber.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability does NOT imprint on source permanent")
    void firstAbilityDoesNotImprint() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Should NOT be tracked in permanentExiledCards
        assertThat(gd.permanentExiledCards.get(chamber.getId())).isNull();
    }

    @Test
    @DisplayName("First ability rejects non-artifact card as target")
    void firstAbilityRejectsNonArtifact() {
        Permanent chamber = addChamberReady(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("First ability rejects target not in any graveyard")
    void firstAbilityRejectsTargetNotInGraveyard() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("First ability fizzles if target removed from graveyard before resolution")
    void firstAbilityFizzlesIfTargetRemoved() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);

        // Remove target before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No charge counter added since exile fizzled
        assertThat(chamber.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("First ability taps the artifact")
    void firstAbilityTaps() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);

        assertThat(chamber.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability without enough mana")
    void cannotActivateFirstAbilityWithoutMana() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        Permanent chamber = addChamberReady(player1);
        Card rod1 = new RodOfRuin();
        Card rod2 = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod1, rod2)));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        // First activation
        harness.activateAbility(player1, chamberIndex, 0, null, rod1.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Untap for second activation
        chamber.untap();
        harness.activateAbility(player1, chamberIndex, 0, null, rod2.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(chamber.getChargeCounters()).isEqualTo(2);
    }

    // ===== Second ability — token creation =====

    @Test
    @DisplayName("Activating second ability with 1 charge counter creates a 3/3 Golem artifact creature token")
    void secondAbilityCreatesGolemToken() {
        Permanent chamber = addChamberReady(player1);
        chamber.setChargeCounters(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(chamber.getChargeCounters()).isEqualTo(0);

        // 3/3 Golem artifact creature token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Cannot activate second ability without charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent chamber = addChamberReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability without enough mana")
    void cannotActivateSecondAbilityWithoutMana() {
        Permanent chamber = addChamberReady(player1);
        chamber.setChargeCounters(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability taps the artifact")
    void secondAbilityTaps() {
        Permanent chamber = addChamberReady(player1);
        chamber.setChargeCounters(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);

        assertThat(chamber.isTapped()).isTrue();
    }

    @Test
    @DisplayName("With multiple charge counters, second ability only removes 1")
    void secondAbilityRemovesExactlyOneCounter() {
        Permanent chamber = addChamberReady(player1);
        chamber.setChargeCounters(3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(chamber.getChargeCounters()).isEqualTo(2);
    }

    // ===== Both abilities interaction =====

    @Test
    @DisplayName("Cannot activate either ability when tapped")
    void cannotActivateWhenTapped() {
        Permanent chamber = addChamberReady(player1);
        chamber.setChargeCounters(1);
        chamber.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Full flow: exile artifact, then create token")
    void fullFlowExileThenCreateToken() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        // Step 1: Exile artifact from graveyard
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(chamber.getChargeCounters()).isEqualTo(1);

        // Step 2: Untap and create token
        chamber.untap();
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(chamber.getChargeCounters()).isEqualTo(0);

        // Golem token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().hasType(CardType.ARTIFACT));
    }

    // ===== Helper methods =====

    private Permanent addChamberReady(Player player) {
        ConversionChamber card = new ConversionChamber();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
