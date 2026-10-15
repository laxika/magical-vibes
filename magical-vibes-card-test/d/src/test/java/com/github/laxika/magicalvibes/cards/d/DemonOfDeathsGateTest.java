package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonOfDeathsGate.class, ChildOfNight.class, BlackKnight.class,
        ReassemblingSkeleton.class, RuneclawBear.class})
class DemonOfDeathsGateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using alternate cost: sacrifice 3 black creatures and pay 6 life")
    void castWithAlternateCost() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        UUID skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton()).getId();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, skeleton));
        harness.passBothPriorities();

        // Demon should be on battlefield
        harness.assertOnBattlefield(player1, "Demon of Death's Gate");

        // Three creatures should be gone from battlefield
        harness.assertNotOnBattlefield(player1, "Child of Night");
        harness.assertNotOnBattlefield(player1, "Black Knight");
        harness.assertNotOnBattlefield(player1, "Reassembling Skeleton");

        // Three creatures should be in graveyard
        harness.assertInGraveyard(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Black Knight");
        harness.assertInGraveyard(player1, "Reassembling Skeleton");

        // Life should be reduced by 6
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can be cast normally with mana")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.addMana(player1, ManaColor.BLACK, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demon of Death's Gate");
    }

    @Test
    @DisplayName("Alternate cost fails if fewer than 3 creatures sacrificed")
    void alternateCostFailsWithFewerCreatures() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice exactly 3");
    }

    @Test
    @DisplayName("Alternate cost fails if non-black creature is sacrificed")
    void alternateCostFailsWithNonBlackCreature() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        UUID bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).getId();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, bears)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails if not enough life (5 life, cost 6)")
    void alternateCostFailsWithInsufficientLife() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        UUID skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton()).getId();

        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, skeleton)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Alternate cost can be paid at exactly 6 life, then its controller loses at 0 life")
    void alternateCostSucceedsAtExactLife() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        UUID skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton()).getId();

        harness.setLife(player1, 6);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, skeleton));

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertNotOnBattlefield(player1, "Demon of Death's Gate");
    }

    @Test
    @DisplayName("Alternate cost does not spend mana")
    void alternateCostDoesNotSpendMana() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        UUID skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton()).getId();

        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, skeleton));

        // Mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("The same creature cannot pay multiple parts of the sacrifice cost")
    void alternateCostRejectsDuplicateCreatures() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new ReassemblingSkeleton());
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(child, child, child))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertOnBattlefield(player1, "Black Knight");
        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Opponent's black creatures cannot pay the sacrifice cost")
    void alternateCostRejectsOpponentsCreature() {
        UUID child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight()).getId();
        UUID knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight()).getId();
        harness.addToBattlefield(player1, new ReassemblingSkeleton());
        UUID opponent = harness.addToBattlefieldAndReturn(player2, new ChildOfNight()).getId();
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(child, knight, opponent))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertOnBattlefield(player1, "Black Knight");
        harness.assertOnBattlefield(player2, "Child of Night");
    }

    @Test
    @DisplayName("Tapped creatures with summoning sickness can pay the sacrifice cost")
    void alternateCostAcceptsTappedCreatures() {
        var child = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        var knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight());
        var skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton());
        child.tap();
        knight.tap();
        skeleton.tap();
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0,
                List.of(child.getId(), knight.getId(), skeleton.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Black Knight");
        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Demon of Death's Gate");
    }
}
