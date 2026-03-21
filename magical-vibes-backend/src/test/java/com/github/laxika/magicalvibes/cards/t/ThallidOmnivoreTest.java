package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThallidOmnivoreTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Thallid Omnivore has two activated abilities")
    void hasTwoActivatedAbilities() {
        ThallidOmnivore card = new ThallidOmnivore();
        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability: sacrifice a Saproling for +2/+2 and 2 life")
    void firstAbilityStructure() {
        ThallidOmnivore card = new ThallidOmnivore();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.getEffects()).hasSize(3);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSubtypeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);
        assertThat(ability.getEffects().get(2)).isInstanceOf(GainLifeEffect.class);

        SacrificeSubtypeCreatureCost sacCost = (SacrificeSubtypeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.subtype()).isEqualTo(CardSubtype.SAPROLING);

        GainLifeEffect lifeGain = (GainLifeEffect) ability.getEffects().get(2);
        assertThat(lifeGain.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability: sacrifice another creature for +2/+2")
    void secondAbilityStructure() {
        ThallidOmnivore card = new ThallidOmnivore();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);

        SacrificeCreatureCost sacCost = (SacrificeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.excludeSelf()).isTrue();
    }

    // ===== Sacrifice a Saproling: +2/+2 and life gain =====

    @Test
    @DisplayName("Sacrificing a Saproling via ability 0 gives +2/+2 and 2 life")
    void sacrificeSaprolingGivesBoostAndLifeGain() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        // Ability 0 = sacrifice Saproling; only 1 Saproling → auto-sacrifice
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saproling"));

        Permanent omnivore = findOmnivore(gd, player1);
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== Sacrifice a non-Saproling creature: +2/+2, no life =====

    @Test
    @DisplayName("Sacrificing a non-Saproling creature via ability 1 gives +2/+2 but no life gain")
    void sacrificeNonSaprolingGivesBoostNoLifeGain() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        // Ability 1 = sacrifice another creature; only 1 other creature → auto-sacrifice
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        Permanent omnivore = findOmnivore(gd, player1);
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Cannot sacrifice itself =====

    @Test
    @DisplayName("Cannot sacrifice Thallid Omnivore to its own second ability (excludeSelf)")
    void cannotSacrificeItself() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Ability 1 with excludeSelf; only 1 other creature → auto-sacrifices Bears
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thallid Omnivore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Mana cost required =====

    @Test
    @DisplayName("Ability requires {1} mana to activate")
    void abilityRequiresMana() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent omnivore = findOmnivore(harness.getGameData(), player1);
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(omnivore.getPowerModifier()).isEqualTo(0);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Can activate both abilities across turns of sacrifice")
    void canActivateBothAbilities() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        // Ability 0: sacrifice Saproling → +2/+2 and 2 life (auto-sacrifice, only 1 Saproling)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Ability 1: sacrifice Bears → +2/+2 (auto-sacrifice, only 1 other creature left)
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent omnivore = findOmnivore(gd, player1);
        assertThat(omnivore.getPowerModifier()).isEqualTo(4);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(4);

        // Only gained 2 life (from Saproling sacrifice)
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== Helper methods =====

    private Permanent addThallidOmnivoreReady(Player player) {
        ThallidOmnivore card = new ThallidOmnivore();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createSaprolingToken() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        return card;
    }

    private Permanent findOmnivore(GameData gd, Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thallid Omnivore"))
                .findFirst().orElseThrow();
    }
}
