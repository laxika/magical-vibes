package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragToTheBottom.class, AvatarOfMight.class, GrizzlyBears.class, Forest.class, Island.class, Plains.class})
class DragToTheBottomTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -1/-1 plus the caster's Domain count")
    void debuffsAllCreaturesByDomainCount() {
        Permanent ownAvatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        Permanent opposingAvatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        castDragToTheBottom();

        assertThat(ownAvatar.getEffectivePower()).isEqualTo(4);
        assertThat(ownAvatar.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingAvatar.getEffectivePower()).isEqualTo(4);
        assertThat(opposingAvatar.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts distinct basic land types controlled by the caster")
    void countsDistinctTypesControlledByCaster() {
        Permanent ownAvatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        Permanent opposingAvatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Plains());

        castDragToTheBottom();

        assertThat(ownAvatar.getEffectivePower()).isEqualTo(6);
        assertThat(ownAvatar.getEffectiveToughness()).isEqualTo(6);
        assertThat(opposingAvatar.getEffectivePower()).isEqualTo(6);
        assertThat(opposingAvatar.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Kills creatures whose toughness is reduced to zero")
    void killsSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        castDragToTheBottom();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new Forest());

        castDragToTheBottom();
        assertThat(avatar.getEffectivePower()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avatar.getEffectivePower()).isEqualTo(8);
        assertThat(avatar.getEffectiveToughness()).isEqualTo(8);
    }

    private void castDragToTheBottom() {
        harness.setHand(player1, List.of(new DragToTheBottom()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
