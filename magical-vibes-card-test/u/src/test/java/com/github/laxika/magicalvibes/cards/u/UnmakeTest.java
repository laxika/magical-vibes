package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unmake.class, GrizzlyBears.class, Forest.class})
class UnmakeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature (not to graveyard)")
    void exilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can exile your own creature using only black mana")
    void exilesOwnCreatureWithBlackMana() {
        GrizzlyBears creature = new GrizzlyBears();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, creature).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(creature);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
            assertThat(entry.faceDown()).isFalse();
        });
        harness.assertInGraveyard(player1, "Unmake");
    }

    @Test
    @DisplayName("Mixed white and black mana exiles only the chosen creature")
    void mixedManaExilesOnlyTarget() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears other = new GrizzlyBears();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, target).getId();
        UUID otherId = harness.addToBattlefieldAndReturn(player2, other).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getId()).containsExactly(otherId);
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(target);
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
            assertThat(entry.faceDown()).isFalse();
        });
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Unmake");
    }
}
