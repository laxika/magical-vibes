package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoungRedDragon.class, BatheInGold.class})
class YoungRedDragonTest extends BaseCardTest {

    @Test
    void adventureCreatesTreasureAndExilesTheCard() {
        YoungRedDragon card = new YoungRedDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAndCannotBlock() {
        YoungRedDragon card = new YoungRedDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        addCreatureReady(player2, new YoungRedDragon());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player1, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void castingCreatureDirectlyDoesNotCreateTreasureOrExileIt() {
        YoungRedDragon card = new YoungRedDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Young Red Dragon");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureRequiresRedMana() {
        harness.setHand(player1, List.of(new YoungRedDragon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Young Red Dragon");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureCanBeCastOnOpponentsTurnAndCreatesExactlyOneUntappedTreasure() {
        YoungRedDragon card = new YoungRedDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Treasure");
        harness.assertNotOnBattlefield(player1, "Young Red Dragon");
        harness.assertNotInGraveyard(player1, "Young Red Dragon");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureCanAttackDespiteBeingUnableToBlock() {
        addCreatureReady(player1, new YoungRedDragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
