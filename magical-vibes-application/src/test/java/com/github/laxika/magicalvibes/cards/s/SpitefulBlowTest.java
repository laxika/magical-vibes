package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpitefulBlow.class, GoldenHind.class, ManaConfluence.class})
class SpitefulBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and target land")
    void destroysTargetCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        cast(creature, land);

        harness.assertNotOnBattlefield(player2, "Golden Hind");
        harness.assertInGraveyard(player2, "Golden Hind");
        harness.assertNotOnBattlefield(player2, "Mana Confluence");
        harness.assertInGraveyard(player2, "Mana Confluence");
    }

    @Test
    @DisplayName("Rejects a noncreature first target")
    void rejectsNoncreatureFirstTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Rejects a nonland second target")
    void rejectsNonlandSecondTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Destroys the remaining target when the other target is gone")
    void destroysRemainingTargetWhenOtherTargetIsGone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        prepareToCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        harness.getGameData().playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mana Confluence");
        harness.assertInGraveyard(player2, "Mana Confluence");
    }

    @Test
    @DisplayName("Requires both targets when casting")
    void requiresBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys targets controlled by different players")
    void destroysTargetsWithDifferentControllers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        cast(creature, land);

        harness.assertInGraveyard(player1, "Golden Hind");
        harness.assertInGraveyard(player2, "Mana Confluence");
    }

    @Test
    @DisplayName("Destroys the creature when the land target is gone")
    void destroysCreatureWhenLandTargetIsGone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        prepareToCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Golden Hind");
        harness.assertInGraveyard(player1, "Spiteful Blow");
    }

    @Test
    @DisplayName("Does not resolve when both targets are gone")
    void doesNotResolveWhenBothTargetsAreGone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ManaConfluence());
        prepareToCast();
        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(creature, land));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiteful Blow");
        harness.assertNotInGraveyard(player2, "Golden Hind");
        harness.assertNotInGraveyard(player2, "Mana Confluence");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new SpitefulBlow()));
        harness.addMana(player1, ManaColor.BLACK, 6);
    }

    private void cast(Permanent creature, Permanent land) {
        prepareToCast();
        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId(), land.getId()));
    }
}
