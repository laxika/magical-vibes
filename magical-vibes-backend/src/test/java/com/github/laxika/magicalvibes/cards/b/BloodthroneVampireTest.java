package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodthroneVampireTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bloodthrone Vampire has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        BloodthroneVampire card = new BloodthroneVampire();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) ability.getEffects().get(1);
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bloodthrone Vampire puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BloodthroneVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bloodthrone Vampire");
    }

    @Test
    @DisplayName("Resolving Bloodthrone Vampire puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BloodthroneVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bloodthrone Vampire"));
    }

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent vampPerm = addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Bloodthrone Vampire should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bloodthrone Vampire"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Bloodthrone Vampire");
        assertThat(entry.getTargetPermanentId()).isEqualTo(vampPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Bloodthrone Vampire +2/+2")
    void resolvingAbilityBoostsVampire() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Bloodthrone Vampire");
        assertThat(vamp.getPowerModifier()).isEqualTo(2);
        assertThat(vamp.getToughnessModifier()).isEqualTo(2);
        assertThat(vamp.getEffectivePower()).isEqualTo(3);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, tokenId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Bloodthrone Vampire");
        assertThat(vamp.getPowerModifier()).isEqualTo(4);
        assertThat(vamp.getToughnessModifier()).isEqualTo(4);
        assertThat(vamp.getEffectivePower()).isEqualTo(5);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(5);

        // Both sacrificed creatures should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saproling Token"));
    }

    @Test
    @DisplayName("Can sacrifice Bloodthrone Vampire to its own ability")
    void canSacrificeItself() {
        addBloodthroneVampireReady(player1);
        UUID vampId = harness.getPermanentId(player1, "Bloodthrone Vampire");

        harness.activateAbility(player1, 0, null, vampId);

        GameData gd = harness.getGameData();

        // Vampire should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bloodthrone Vampire"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bloodthrone Vampire"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bloodthrone Vampire");
    }

    @Test
    @DisplayName("Boost fizzles when Vampire sacrifices itself")
    void boostFizzlesWhenVampireSacrificesItself() {
        addBloodthroneVampireReady(player1);
        UUID vampId = harness.getPermanentId(player1, "Bloodthrone Vampire");

        harness.activateAbility(player1, 0, null, vampId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Vampire is in the graveyard, ability fizzled — no crash
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bloodthrone Vampire"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bloodthrone Vampire"));
    }

    // ===== No mana cost =====

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Bloodthrone Vampire")
    void activatingAbilityDoesNotTap() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.isTapped()).isFalse();
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getEffectivePower()).isEqualTo(3);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(3);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vamp.getPowerModifier()).isEqualTo(0);
        assertThat(vamp.getToughnessModifier()).isEqualTo(0);
        assertThat(vamp.getEffectivePower()).isEqualTo(1);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addBloodthroneVampireReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentCreature() {
        addBloodthroneVampireReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature you control");
    }

    // ===== Helper methods =====

    private Permanent addBloodthroneVampireReady(Player player) {
        BloodthroneVampire card = new BloodthroneVampire();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
