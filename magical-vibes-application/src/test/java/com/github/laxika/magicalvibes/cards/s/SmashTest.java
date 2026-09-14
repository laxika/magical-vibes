package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.d.DragonArch;
import com.github.laxika.magicalvibes.cards.z.ZombieBoa;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({Smash.class, DragonArch.class, Dodecapod.class, ZombieBoa.class})
class SmashTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Smash puts it on the stack with target")
    void castingPutsOnStack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonArch());
        Smash smash = new Smash();
        harness.setHand(player1, List.of(smash));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(smash);
        assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Resolving Smash destroys target artifact and draws a card")
    void destroysArtifactAndDraws() {
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonArch());
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Dragon Arch");
        harness.assertInGraveyard(player2, "Dragon Arch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Resolving Smash destroys an artifact creature and draws a card")
    void destroysArtifactCreatureAndDraws() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Dodecapod());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can destroy own artifact with Smash")
    void canDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Dodecapod());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dodecapod");
        harness.assertInGraveyard(player1, "Dodecapod");
    }

    @Test
    @DisplayName("Smash goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonArch());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Smash");
    }

    @Test
    @DisplayName("Smash fizzles and does not draw when target is removed before resolution")
    void fizzlesAndDoesNotDrawWhenTargetRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonArch());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Smash");
    }

    @Test
    @DisplayName("Cannot destroy a creature with Smash")
    void cannotDestroyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZombieBoa());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

