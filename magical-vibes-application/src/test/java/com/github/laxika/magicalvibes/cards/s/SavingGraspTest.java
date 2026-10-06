package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({SavingGrasp.class, DawntreaderElk.class, EvolvingWilds.class})
class SavingGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Saving Grasp puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Saving Grasp");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you own");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Evolving Wilds");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you own");
    }

    @Test
    @DisplayName("Cannot target stolen creature you control but do not own")
    void cannotTargetStolenCreature() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        DawntreaderElk stolenBears = new DawntreaderElk();
        UUID stolenId = harness.addToBattlefieldAndReturn(player1, stolenBears).getId();
        gd.stolenCreatures.put(stolenId, player2.getId());

        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, stolenId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you own");
    }

    @Test
    @DisplayName("Resolving returns own creature to hand")
    void resolvingReturnsOwnCreatureToHand() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        harness.assertInHand(player1, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Saving Grasp goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Saving Grasp");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dawntreader Elk"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Saving Grasp");
    }

    @Test
    @DisplayName("Flashback from graveyard returns own creature to hand")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        harness.assertInHand(player1, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Saving Grasp");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saving Grasp"));
    }
    @Test
    @DisplayName("Returns a creature you own from an opponent's battlefield to your hand")
    void returnsOwnedCreatureControlledByOpponent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk()).getId();
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertInHand(player1, "Dawntreader Elk");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A change in control does not invalidate an owned creature target")
    void targetRemainsLegalAfterControlChanges() {
        var target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertInHand(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player1, "Saving Grasp");
    }

    @Test
    @DisplayName("Flashback exiles Saving Grasp even when its target disappears")
    void flashbackExilesWhenTargetDisappears() {
        var target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setGraveyard(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFlashback(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Saving Grasp");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saving Grasp"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dawntreader Elk"));
    }
}
