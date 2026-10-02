package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.cards.r.Resize;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flashfreeze.class, BorealDruid.class, FrostRaptor.class, Resize.class, RiteOfFlame.class, RagingGoblin.class})
class FlashfreezeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a green spell")
    void castingPutsOnStackTargetingGreenSpell() {
        BorealDruid druid = new BorealDruid();
        harness.castFromHand(player1, druid, "{G}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry flashfreezeEntry = gd.stack.getLast();
        assertThat(flashfreezeEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(flashfreezeEntry.getCard().getName()).isEqualTo("Flashfreeze");
        assertThat(flashfreezeEntry.getTargetId()).isEqualTo(druid.getId());
    }

    @Test
    @DisplayName("Cannot target a blue spell")
    void cannotTargetBlueSpell() {
        FrostRaptor raptor = new FrostRaptor();
        harness.castFromHand(player1, raptor, "{2}{U}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, raptor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a green creature spell")
    void countersGreenCreatureSpell() {
        BorealDruid druid = new BorealDruid();
        harness.castFromHand(player1, druid, "{G}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Boreal Druid");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Boreal Druid");
    }

    @Test
    @DisplayName("Resolving counters a red sorcery spell")
    void countersRedSorcerySpell() {
        RiteOfFlame rite = new RiteOfFlame();
        harness.castFromHand(player1, rite, "{R}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rite.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rite of Flame");
    }

    @Test
    @DisplayName("Resolving counters a green instant spell")
    void countersGreenInstantSpell() {
        BorealDruid druid = new BorealDruid();
        harness.addToBattlefield(player1, druid);

        Resize resize = new Resize();
        harness.setHand(player1, List.of(resize));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Boreal Druid"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, resize.getId());
        harness.passBothPriorities();

        // Resize countered and in graveyard
        harness.assertInGraveyard(player1, "Resize");
    }

    @Test
    @DisplayName("Flashfreeze goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        BorealDruid druid = new BorealDruid();
        harness.castFromHand(player1, druid, "{G}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Flashfreeze");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving counters a red creature spell")
    void countersRedCreatureSpell() {
        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, goblin.getId());

        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        BorealDruid druid = new BorealDruid();
        harness.castFromHand(player1, druid, "{G}");

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());

        // Remove target from stack before Flashfreeze resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(druid.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Flashfreeze still goes to graveyard
        harness.assertInGraveyard(player2, "Flashfreeze");
    }
}

