package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SirenStormtamerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Siren Stormtamer has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        SirenStormtamer card = new SirenStormtamer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{U}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsSpellTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(CounterSpellEffect.class);
        assertThat(ability.getTargetFilter()).isInstanceOf(StackEntryPredicateTargetFilter.class);
        var filter = (StackEntryPredicateTargetFilter) ability.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(StackEntryAllOfPredicate.class);
        var allOf = (StackEntryAllOfPredicate) filter.predicate();
        assertThat(allOf.predicates()).hasSize(2);
        assertThat(allOf.predicates().get(0)).isInstanceOf(StackEntryHasTargetPredicate.class);
        assertThat(allOf.predicates().get(1)).isInstanceOf(StackEntryTargetsYouOrCreatureYouControlPredicate.class);
    }

    // ===== Counter spell targeting a creature you control =====

    @Test
    @DisplayName("Counters a spell targeting a creature you control")
    void countersSpellTargetingYourCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Shock targeting player1's Grizzly Bears
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        // Player1 activates Siren Stormtamer's ability targeting Shock
        harness.activateAbility(player1, 1, null, shock.getId());

        // Resolve the counter ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock should be countered (in player2's graveyard)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));

        // Grizzly Bears should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Siren Stormtamer should be in player1's graveyard (sacrificed)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Siren Stormtamer"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Siren Stormtamer"));
    }

    // ===== Counter spell targeting you (the player) =====

    @Test
    @DisplayName("Counters a spell targeting you (the player)")
    void countersSpellTargetingYou() {
        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Shock targeting player1 (the player)
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Player1 activates Siren Stormtamer's ability targeting Shock
        harness.activateAbility(player1, 0, null, shock.getId());

        // Resolve the counter ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock should be countered
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));

        // Player1 life should be untouched (20)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        // Stormtamer should be sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Siren Stormtamer"));
    }

    // ===== Cannot counter a spell that doesn't target you or your creatures =====

    @Test
    @DisplayName("Cannot target a spell that targets opponent's creature")
    void cannotTargetSpellTargetingOpponentsCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player1 casts Shock targeting player2's Grizzly Bears
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        // Player1 tries to activate Stormtamer targeting their own Shock — should fail
        // (the Shock targets player2's creature, not player1 or a creature player1 controls)
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-targeting creature spell")
    void cannotTargetNonTargetingSpell() {
        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Grizzly Bears (no target)
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        // Player1 tries to activate Stormtamer — should fail (creature spell has no target)
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Counter an activated ability =====

    @Test
    @DisplayName("Counters an activated ability targeting a creature you control")
    void countersActivatedAbilityTargetingYourCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 activates Fume Spitter's ability targeting player1's Grizzly Bears
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        // Player1 activates Siren Stormtamer's ability targeting Fume Spitter's ability
        harness.activateAbility(player1, 1, null, fumeSpitter.getId());

        // Resolve the counter ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Fume Spitter's ability should be countered — Grizzly Bears survives without -1/-1 counter
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Fume Spitter should be in graveyard (sacrificed as cost before countering)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fume Spitter"));

        // Siren Stormtamer should be in graveyard (sacrificed as cost)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Siren Stormtamer"));

        // Stack should be empty
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target spell is removed from the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);
        harness.activateAbility(player1, 1, null, shock.getId());

        // Remove target spell before ability resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Shock"));

        harness.passBothPriorities();

        // Ability should fizzle
        assertThat(gd.stack).isEmpty();

        // Stormtamer is still sacrificed (cost was already paid)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Siren Stormtamer"));
    }

    // ===== Mana validation =====

    @Test
    @DisplayName("Cannot activate ability without {U} mana")
    void cannotActivateWithoutBlueMana() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        // Player1 has no mana

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Counter spell targeting opponent (player) — cannot use =====

    @Test
    @DisplayName("Cannot target a spell targeting the opponent player")
    void cannotTargetSpellTargetingOpponentPlayer() {
        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player1, stormtamer);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player1 casts Shock targeting player2 (the opponent)
        harness.castInstant(player1, 0, player2.getId());

        // Player1 tries to activate Stormtamer — should fail
        // (Shock targets player2, not player1 or a creature player1 controls)
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
