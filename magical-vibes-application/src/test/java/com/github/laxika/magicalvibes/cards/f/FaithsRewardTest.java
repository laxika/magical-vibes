package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaithsRewardTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void castFaithsReward() {
        harness.setHand(player1, List.of(new FaithsReward()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);
        resolveUntilInputOrEmpty();
    }

    @Test
    @DisplayName("Returns a creature and an artifact that died this turn to the battlefield")
    void returnsPermanentsThatDiedThisTurn() {
        Card bears = new GrizzlyBears();
        Card axe = new ObsidianBattleAxe();
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player1, axe);

        harness.setHand(player1, List.of(new DoomBlade(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Obsidian Battle-Axe");

        castFaithsReward();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Obsidian Battle-Axe");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()) || c.getId().equals(axe.getId()));
    }

    @Test
    @DisplayName("Does not return cards that were already in the graveyard")
    void doesNotReturnCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castFaithsReward();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not return a nonpermanent card put into the graveyard this turn")
    void doesNotReturnInstantCard() {
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        castFaithsReward();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doom Blade"));
    }
}
