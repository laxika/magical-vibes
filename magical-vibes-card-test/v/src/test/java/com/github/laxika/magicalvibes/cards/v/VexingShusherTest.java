package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VexingShusher.class, Cancel.class, GrizzlyBears.class})
class VexingShusherTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes a target spell can't be countered, so a counterspell fails")
    void abilityProtectsTargetSpellFromCounter() {
        VexingShusher shusher = new VexingShusher();
        harness.addToBattlefield(player1, shusher);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G} for the bears + {R/G} for the ability

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3); // {1}{U}{U}

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);

        // Player2 responds with Cancel targeting the Grizzly Bears spell.
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());

        // Player1 makes the Grizzly Bears spell uncounterable (resolves before Cancel).
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cancel resolved but couldn't counter — Grizzly Bears entered the battlefield.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vexing Shusher itself can't be countered")
    void selfCannotBeCountered() {
        VexingShusher shusher = new VexingShusher();
        harness.setHand(player1, List.of(shusher));
        harness.addMana(player1, ManaColor.GREEN, 2); // {R/G}{R/G}

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, shusher.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertOnBattlefield(player1, "Vexing Shusher");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability cannot be activated without a spell on the stack")
    void abilityRequiresSpellTarget() {
        VexingShusher shusher = new VexingShusher();
        harness.addToBattlefield(player1, shusher);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red mana can protect an opponent's instant spell")
    void protectsOpponentsInstantWithRedMana() {
        harness.addToBattlefield(player1, new VexingShusher());
        GrizzlyBears bears = new GrizzlyBears();
        Cancel opposingCancel = new Cancel();
        harness.setHand(player1, List.of(bears, new Cancel()));
        harness.setHand(player2, List.of(opposingCancel));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, opposingCancel.getId());
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, opposingCancel.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An activated ability on the stack is not a spell target")
    void cannotTargetActivatedAbility() {
        harness.addToBattlefield(player1, new VexingShusher());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, bears.getId());
        UUID abilityId = harness.getGameData().stack.getLast().getTargetableId();

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A counterspell responding to the ability can counter the spell before protection resolves")
    void protectionOnlyAppliesAfterAbilityResolves() {
        harness.addToBattlefield(player1, new VexingShusher());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
