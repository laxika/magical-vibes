package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thwart.class, CloudSprite.class, Island.class})
class ThwartTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell for its mana cost")
    void countersTargetSpell() {
        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");

        harness.setHand(player2, List.of(new Thwart()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, sprite.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cloud Sprite");
        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
        harness.assertInGraveyard(player2, "Thwart");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Counters a spell by returning three Islands to hand")
    void countersTargetSpellWithAlternateCost() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent thirdIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");

        harness.setHand(player2, List.of(new Thwart()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, sprite.getId(),
                List.of(firstIsland.getId(), secondIsland.getId(), thirdIsland.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cloud Sprite");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    @DisplayName("Alternate cost requires three Islands")
    void alternateCostRequiresThreeIslands() {
        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");

        List<Permanent> islands = List.of(
                harness.addToBattlefieldAndReturn(player2, new Island()),
                harness.addToBattlefieldAndReturn(player2, new Island()));

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Thwart()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, sprite.getId(),
                islands.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Island permanent")
    void alternateCostRejectsNonIsland() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent nonIsland = harness.addToBattlefieldAndReturn(player2, new CloudSprite());

        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Thwart()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, sprite.getId(),
                List.of(firstIsland.getId(), nonIsland.getId(), secondIsland.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Alternate cost requires Islands controlled by the caster")
    void alternateCostRequiresIslandsTheCasterControls() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Thwart()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, sprite.getId(),
                List.of(firstIsland.getId(), opposingIsland.getId(), secondIsland.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Alternate cost rejects returning the same Island twice")
    void alternateCostRejectsReturningTheSameIslandTwice() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        CloudSprite sprite = new CloudSprite();
        harness.castFromHand(player1, sprite, "{U}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Thwart()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, sprite.getId(),
                List.of(firstIsland.getId(), firstIsland.getId(), secondIsland.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }
}
