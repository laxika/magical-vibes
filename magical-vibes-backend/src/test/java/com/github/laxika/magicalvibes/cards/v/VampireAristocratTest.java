package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VampireAristocratTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vampire Aristocrat has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        VampireAristocrat card = new VampireAristocrat();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) ability.getEffects().get(1);
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent vampPerm = addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Vampire Aristocrat should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(entry.getTargetId()).isEqualTo(vampPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Vampire Aristocrat +2/+2")
    void resolvingAbilityBoostsVampire() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(vamp.getPowerModifier()).isEqualTo(2);
        assertThat(vamp.getToughnessModifier()).isEqualTo(2);
        assertThat(vamp.getEffectivePower()).isEqualTo(4);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tokenId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent vamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getCard().getName()).isEqualTo("Vampire Aristocrat");
        assertThat(vamp.getPowerModifier()).isEqualTo(4);
        assertThat(vamp.getToughnessModifier()).isEqualTo(4);
        assertThat(vamp.getEffectivePower()).isEqualTo(6);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can sacrifice Vampire Aristocrat to its own ability")
    void canSacrificeItself() {
        addVampireAristocratReady(player1);
        UUID vampId = harness.getPermanentId(player1, "Vampire Aristocrat");

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Vampire should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Aristocrat"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vampire Aristocrat");
    }

    @Test
    @DisplayName("Boost fizzles when Vampire sacrifices itself")
    void boostFizzlesWhenVampireSacrificesItself() {
        addVampireAristocratReady(player1);
        UUID vampId = harness.getPermanentId(player1, "Vampire Aristocrat");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Vampire is in the graveyard, ability fizzled — no crash
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Aristocrat"));
    }

    // ===== No mana cost =====

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Vampire Aristocrat")
    void activatingAbilityDoesNotTap() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.isTapped()).isFalse();
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addVampireAristocratReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent vamp = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vamp.getEffectivePower()).isEqualTo(4);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(4);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vamp.getPowerModifier()).isEqualTo(0);
        assertThat(vamp.getToughnessModifier()).isEqualTo(0);
        assertThat(vamp.getEffectivePower()).isEqualTo(2);
        assertThat(vamp.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Auto-sacrifices when only one creature available")
    void autoSacrificesWhenOnlyOneCreatureAvailable() {
        addVampireAristocratReady(player1);
        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        // Vampire auto-sacrificed (only creature on battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Aristocrat"));
        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helper methods =====

    private Permanent addVampireAristocratReady(Player player) {
        VampireAristocrat card = new VampireAristocrat();
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
