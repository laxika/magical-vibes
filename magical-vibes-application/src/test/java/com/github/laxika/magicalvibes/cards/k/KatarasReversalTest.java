package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VigilantDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KatarasReversal.class, GrizzlyBears.class, AngelsMercy.class, VigilantDrake.class, Island.class})
class KatarasReversalTest extends BaseCardTest {

    @Test
    void countersSpellsAndUntapsArtifactsOrCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature1.tap();
        creature2.tap();

        KatarasReversal reversal = new KatarasReversal();
        harness.setHand(player1, List.of(bears, mercy, reversal));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0,
                List.of(bears.getId(), mercy.getId(), creature1.getId(), creature2.getId()));

        assertThat(harness.getGameData().stack.getLast().getTargetGroupSizes()).containsExactly(2, 2);
        harness.passBothPriorities();

        assertThat(creature1.isTapped()).isFalse();
        assertThat(creature2.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Angel's Mercy");
    }

    @Test
    void countersAnActivatedAbility() {
        VigilantDrake drakeCard = new VigilantDrake();
        harness.addToBattlefield(player1, drakeCard);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(drakeCard.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Katara's Reversal");
    }

    @Test
    void cannotUntapAPlainLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new KatarasReversal()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
