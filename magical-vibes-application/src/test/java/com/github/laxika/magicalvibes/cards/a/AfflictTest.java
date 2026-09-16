package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.n.NantukoDisciple;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Afflict.class, NantukoDisciple.class, DwarvenGrunt.class})
class AfflictTest extends BaseCardTest {

    private void setupDiscipleAndAfflict() {
        harness.addToBattlefield(player1, new NantukoDisciple());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Casting Afflict puts it on the stack as INSTANT_SPELL with target")
    void castingPutsItOnStack() {
        setupDiscipleAndAfflict();
        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Afflict gives -1/-1 to target creature")
    void resolvingGivesMinusOneMinusOne() {
        setupDiscipleAndAfflict();
        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving Afflict draws a card for the caster")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new NantukoDisciple());
        NantukoDisciple deckCard = new NantukoDisciple();
        harness.setLibrary(player1, List.of(deckCard));

        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");
        harness.castInstant(player1, 0, targetId);

        // Hand should be empty after casting (Afflict was the only card)
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        // After resolving, player should have drawn a card
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Debuff wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        setupDiscipleAndAfflict();
        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        harness.castAndResolveInstant(player1, 0, targetId);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal debuff still draws before state-based actions remove the creature")
    void lethalDebuffStillDrawsBeforeCreatureDies() {
        harness.addToBattlefield(player1, new DwarvenGrunt());
        harness.setLibrary(player1, List.of(new NantukoDisciple()));
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Dwarven Grunt");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Spell fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        setupDiscipleAndAfflict();
        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        harness.castInstant(player1, 0, targetId);

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle — no crash, stack should be empty
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new NantukoDisciple());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Nantuko Disciple");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new NantukoDisciple());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with invalid target permanent ID")
    void cannotCastWithInvalidTarget() {
        harness.addToBattlefield(player1, new NantukoDisciple()); // valid target so spell is playable
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Afflict goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupDiscipleAndAfflict();
        UUID targetId = harness.getPermanentId(player1, "Nantuko Disciple");

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Afflict");
    }
}

