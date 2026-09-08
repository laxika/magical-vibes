package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinArtisansTest extends BaseCardTest {

    @Test
    @DisplayName("The coin flip draws on a win and counters the artifact spell on a loss")
    void coinFlipChoosesDrawOrCounter() {
        Permanent artisans = addReadyArtisans(player1);
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        boolean wonFlip = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Artisans"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Goblin Artisans"));

        if (wonFlip) {
            assertThat(gd.stack)
                    .anyMatch(entry -> entry.getCard().getName().equals("Millstone"));
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        } else {
            harness.assertInGraveyard(player1, "Millstone");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
        assertThat(artisans.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact spell cannot be targeted by another Goblin Artisans ability")
    void cannotTargetSpellAlreadyTargetedByAnotherArtisans() {
        addReadyArtisans(player1);
        addReadyArtisans(player1);
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, null, millstone.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("isn't targeted by another Goblin Artisans");
    }

    @Test
    @DisplayName("The ability can target only an artifact spell controlled by its controller")
    void onlyTargetsOwnArtifactSpell() {
        addReadyArtisans(player1);
        Millstone millstone = new Millstone();
        harness.setHand(player2, List.of(millstone));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArtisans(Player player) {
        Permanent permanent = new Permanent(new GoblinArtisans());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
