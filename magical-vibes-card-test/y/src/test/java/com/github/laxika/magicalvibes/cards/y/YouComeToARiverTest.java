package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TreasureChest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouComeToARiver.class, GrizzlyBears.class, Island.class, TreasureChest.class})
class YouComeToARiverTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void boostsTargetCreatureAndMakesItUnblockableUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void neitherModeCanTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");

        harness.setHand(player1, List.of(new YouComeToARiver()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void returnsANoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreasureChest());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Treasure Chest");
        harness.assertInHand(player2, "Treasure Chest");
    }

    @Test
    void crossingCanTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void crossingDoesNotAffectACreatureReturnedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "You Come to a River");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.isCantBeBlocked()).isFalse();
    }
}
