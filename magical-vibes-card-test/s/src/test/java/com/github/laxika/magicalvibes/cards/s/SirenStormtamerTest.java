package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WatertrapWeaver;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SirenStormtamer.class, Shock.class, GrizzlyBears.class, FumeSpitter.class, WatertrapWeaver.class})
class SirenStormtamerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a triggered ability without removing its source")
    void countersTriggeredAbility() {
        SirenStormtamer protectedCreature = new SirenStormtamer();
        harness.addToBattlefield(player1, protectedCreature);
        harness.addToBattlefield(player1, new SirenStormtamer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new WatertrapWeaver()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, 0,
                harness.getPermanentId(player1, "Siren Stormtamer"));
        harness.passBothPriorities();

        var triggerId = gd.stack.getFirst().getTargetableId();
        harness.activateAbility(player1, 1, null, triggerId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Watertrap Weaver");
        var survivor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(survivor.isTapped()).isFalse();
        assertThat(survivor.getSkipUntapCount()).isZero();
        harness.assertInGraveyard(player1, "Siren Stormtamer");
    }

    @Test
    @DisplayName("Can counter your own spell targeting you")
    void countersOwnSpellTargetingYou() {
        harness.addToBattlefield(player1, new SirenStormtamer());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Siren Stormtamer");
    }

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

        // Shock should be countered (in player2's graveyard)
        harness.assertInGraveyard(player2, "Shock");

        // Grizzly Bears should still be alive
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Siren Stormtamer should be in player1's graveyard (sacrificed)
        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.assertNotOnBattlefield(player1, "Siren Stormtamer");
    }

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

        // Shock should be countered
        harness.assertInGraveyard(player2, "Shock");

        // Player1 life should be untouched (20)
        harness.assertLife(player1, 20);

        // Stormtamer should be sacrificed
        harness.assertInGraveyard(player1, "Siren Stormtamer");
    }

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
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Fume Spitter should be in graveyard (sacrificed as cost before countering)
        harness.assertInGraveyard(player2, "Fume Spitter");

        // Siren Stormtamer should be in graveyard (sacrificed as cost)
        harness.assertInGraveyard(player1, "Siren Stormtamer");

        // Stack should be empty
        assertThat(gd.stack).isEmpty();
    }

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
        harness.assertInGraveyard(player1, "Siren Stormtamer");
    }

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
