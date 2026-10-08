package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VithianRenegades.class, RodOfRuin.class, GrizzlyBears.class, DarksteelPlate.class, Terminate.class})
class VithianRenegadesTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Casting Vithian Renegades puts it on the stack with target")
    void castingPutsOnStackWithTarget() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving enters battlefield and puts ETB destroy trigger on the stack")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Vithian Renegades");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys the target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible artifact survives the ETB destroy")
    void indestructibleArtifactSurvives() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB

        harness.assertOnBattlefield(player2, "Darksteel Plate");
    }

    @Test
    @DisplayName("ETB fizzles if the target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature -> ETB on stack

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB -> fizzles

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Creature enters without an ability on the stack when no legal artifact target exists")
    void entersWithoutLegalArtifactTarget() {
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Vithian Renegades");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Must destroy your own artifact when it is the only legal target")
    void destroysControllersOnlyArtifact() {
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Rod of Ruin"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rod of Ruin");
        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertOnBattlefield(player1, "Vithian Renegades");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target an artifact that appears while the creature spell is on the stack")
    void choosesArtifactThatAppearsBeforeEntry() {
        harness.setHand(player1, List.of(new VithianRenegades()));
        giveMana();
        harness.castCreature(player1, 0);

        harness.addToBattlefield(player2, new RodOfRuin());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Rod of Ruin"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertOnBattlefield(player1, "Vithian Renegades");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroy trigger resolves even if Vithian Renegades dies in response")
    void triggerResolvesAfterSourceDies() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VithianRenegades(), new Terminate()));
        giveMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Rod of Ruin"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Vithian Renegades"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Vithian Renegades");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.stack).isEmpty();
    }
}
