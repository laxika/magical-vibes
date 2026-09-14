package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Terminate.class, ThornscapeFamiliar.class, ForsakenCity.class})
class TerminateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Terminate targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThornscapeFamiliar());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        // Add a creature as a valid target so the spell is playable
        harness.addToBattlefield(player1, new ThornscapeFamiliar());

        Permanent land = harness.addToBattlefieldAndReturn(player2, new ForsakenCity());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolving Terminate destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThornscapeFamiliar());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Thornscape Familiar");
        harness.assertInGraveyard(player2, "Thornscape Familiar");
    }

    @Test
    @DisplayName("Terminate ignores regeneration shield because it cannot be regenerated")
    void ignoresRegenerationShield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThornscapeFamiliar());
        creature.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Thornscape Familiar");
        harness.assertInGraveyard(player2, "Thornscape Familiar");
    }

    @Test
    @DisplayName("Terminate does not destroy an indestructible creature")
    void respectsIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThornscapeFamiliar());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Thornscape Familiar");
        harness.assertNotInGraveyard(player2, "Thornscape Familiar");
    }

    @Test
    @DisplayName("Terminate fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThornscapeFamiliar());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Terminate");
    }
}
