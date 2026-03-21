package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MyrWelderTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one activated ability: tap to exile target artifact from a graveyard with imprint")
    void hasCorrectActivatedAbility() {
        MyrWelder card = new MyrWelder();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ExileTargetCardFromGraveyardAndImprintOnSourceEffect.class);

        ExileTargetCardFromGraveyardAndImprintOnSourceEffect effect =
                (ExileTargetCardFromGraveyardAndImprintOnSourceEffect) ability.getEffects().getFirst();
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.filter()).cardType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Has static effect to gain activated abilities of exiled cards")
    void hasCorrectStaticEffect() {
        MyrWelder card = new MyrWelder();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GainActivatedAbilitiesOfExiledCardsEffect.class);
    }

    // ===== Imprint ability — exile target artifact from a graveyard =====

    @Test
    @DisplayName("Imprint ability puts exile effect on the stack (targeting artifact in graveyard)")
    void imprintPutsExileOnStack() {
        Permanent welder = addWelderReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Welder");
        assertThat(welder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles artifact from controller's graveyard on resolution and tracks in permanentExiledCards")
    void exilesArtifactFromOwnGraveyardOnResolution() {
        Permanent welder = addWelderReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod of Ruin should be removed from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));

        // Should be tracked in permanentExiledCards for the welder
        List<Card> exiledWithWelder = gd.permanentExiledCards.get(welder.getId());
        assertThat(exiledWithWelder).isNotNull().hasSize(1);
        assertThat(exiledWithWelder.getFirst().getName()).isEqualTo("Rod of Ruin");

        // Should also be in player exiled cards
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    @DisplayName("Exiles artifact from opponent's graveyard on resolution")
    void exilesArtifactFromOpponentGraveyardOnResolution() {
        Permanent welder = addWelderReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>(List.of(rod)));

        harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod of Ruin should be removed from opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Rod of Ruin"));

        // Should be tracked in permanentExiledCards for the welder
        List<Card> exiledWithWelder = gd.permanentExiledCards.get(welder.getId());
        assertThat(exiledWithWelder).isNotNull().hasSize(1);
        assertThat(exiledWithWelder.getFirst().getName()).isEqualTo("Rod of Ruin");

        // Should be in opponent's exiled cards (cards are owned by their graveyard owner)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    @DisplayName("Rejects non-artifact card as target")
    void rejectsNonArtifactTarget() {
        addWelderReady(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Rejects target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        addWelderReady(player1);
        Card rod = new RodOfRuin();
        // Rod is NOT in any graveyard — just a card object
        harness.setGraveyard(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Fizzles if target is removed from graveyard before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent welder = addWelderReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD);

        // Simulate opponent removing the card from graveyard before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Ability should have fizzled — no card tracked for the welder
        assertThat(gd.permanentExiledCards.get(welder.getId())).isNull();
    }

    // ===== Gaining activated abilities from exiled cards =====

    @Test
    @DisplayName("Gains activated abilities from exiled artifact card")
    void gainsActivatedAbilitiesFromExiledArtifact() {
        Permanent welder = addWelderReady(player1);

        // Directly set up exiled card tracking (simulating after imprint)
        Card rod = new RodOfRuin();
        gd.permanentExiledCards.put(welder.getId(), Collections.synchronizedList(new ArrayList<>(List.of(rod))));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, welder).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{3}");
    }

    @Test
    @DisplayName("Can exile multiple artifacts and gain all their abilities")
    void gainsAbilitiesFromMultipleExiledArtifacts() {
        Permanent welder = addWelderReady(player1);

        // Simulate two exiled artifacts (two copies of Rod of Ruin)
        Card rod1 = new RodOfRuin();
        Card rod2 = new RodOfRuin();
        gd.permanentExiledCards.put(welder.getId(), Collections.synchronizedList(new ArrayList<>(List.of(rod1))));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, welder).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);

        // Add a second Rod of Ruin
        gd.permanentExiledCards.get(welder.getId()).add(rod2);

        granted = gqs.computeStaticBonus(gd, welder).grantedActivatedAbilities();
        assertThat(granted).hasSize(2);
    }

    @Test
    @DisplayName("Does not gain abilities before any card is exiled")
    void noAbilitiesBeforeExile() {
        Permanent welder = addWelderReady(player1);

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, welder).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    // ===== Activating gained abilities =====

    @Test
    @DisplayName("Can activate gained ability from exiled artifact")
    void canActivateGainedAbility() {
        Permanent welder = addWelderReady(player1);

        // Set up exiled Rod of Ruin (has {3}, {T}: deal 1 damage to any target)
        Card rod = new RodOfRuin();
        gd.permanentExiledCards.put(welder.getId(), Collections.synchronizedList(new ArrayList<>(List.of(rod))));

        // Rod's ability needs {3} mana and tap
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // The welder's own ability is index 0, Rod's gained ability is index 1
        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Welder");
        assertThat(welder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving gained damage ability deals damage to target")
    void resolvedGainedAbilityDealsDamage() {
        Permanent welder = addWelderReady(player1);

        // Set up exiled Rod of Ruin
        Card rod = new RodOfRuin();
        gd.permanentExiledCards.put(welder.getId(), Collections.synchronizedList(new ArrayList<>(List.of(rod))));

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Activate gained Rod ability targeting player 2
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Full flow: exile artifact then activate its ability")
    void fullFlowExileThenActivate() {
        Permanent welder = addWelderReady(player1);
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        // Step 1: Activate imprint ability targeting the Rod in graveyard
        harness.activateAbility(player1, 0, 0, null, rod.getId(), Zone.GRAVEYARD);

        // Resolve the imprint ability — exile happens on resolution
        harness.passBothPriorities();

        // Rod should be exiled with the welder
        assertThat(gd.permanentExiledCards.get(welder.getId())).hasSize(1);
        assertThat(gd.permanentExiledCards.get(welder.getId()).getFirst().getName()).isEqualTo("Rod of Ruin");

        // Step 2: Untap the welder (it was tapped for the imprint)
        welder.untap();

        // Step 3: Activate the gained Rod of Ruin ability
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        // Player 2 takes 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Helper methods =====

    private Permanent addWelderReady(Player player) {
        MyrWelder card = new MyrWelder();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
