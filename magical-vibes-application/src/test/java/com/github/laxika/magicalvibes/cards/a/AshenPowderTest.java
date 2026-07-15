package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshenPowderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting targets a creature card in an opponent's graveyard")
    void castingPutsOnStack() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolving puts the creature onto the battlefield untapped under caster's control")
    void resolvesUnderCasterControl() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        assertThat(creature.isTapped()).isFalse();
        // Removed from the opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Tracked as stolen so it returns to its owner on leaving the battlefield
        assertThat(gd.stolenCreatures).containsKey(creature.getId());
    }

    @Test
    @DisplayName("Stolen creature goes to its owner's graveyard when it dies")
    void diesToOwnerGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent creature = findCreatureOnBattlefield(player1.getId(), "Grizzly Bears");
        UUID creatureId = creature.getId();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player2 shocks their own former creature (2 damage kills 2-toughness Grizzly Bears)
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creatureId);

        // Owner (player2) receives the dead creature, not the controller (player1)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature card in your own graveyard")
    void cannotTargetOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in an opponent's graveyard")
    void cannotTargetNonCreature() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new AshenPowder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent findCreatureOnBattlefield(UUID playerId, String cardName) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
