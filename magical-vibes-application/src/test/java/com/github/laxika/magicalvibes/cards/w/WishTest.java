package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wish.class, GrizzlyBears.class, Forest.class})
class WishTest extends BaseCardTest {

    @Test
    @DisplayName("Grants permission to play every current sideboard card this turn")
    void grantsOutsideGamePlayPermission() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(creature, land)));

        castWish();

        assertThat(gd.outsideGamePlayPermissions)
                .containsExactlyInAnyOrder(creature.getId(), land.getId());
    }

    @Test
    @DisplayName("Casts a sideboard creature for its normal mana cost")
    void castsSideboardCreature() {
        Card creature = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(creature)));
        castWish();

        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, creature.getId());

        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(creature.getId());
        assertThat(gd.stack.getFirst().getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Plays a sideboard land and counts it against the land play limit")
    void playsSideboardLand() {
        Card land = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(land)));
        castWish();

        prepareMainPhase();
        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(land.getId());
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Unused outside-the-game permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        Card creature = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(creature)));
        castWish();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.outsideGamePlayPermissions).doesNotContain(creature.getId());
        harness.forceActivePlayer(player1);
        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    private void castWish() {
        harness.castFromHand(player1, new Wish(), "{2}{R}");
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
