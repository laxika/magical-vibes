package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirrorformTest extends BaseCardTest {

    private void castMirrorform(Permanent target) {
        harness.setHand(player1, List.of(new Mirrorform()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Each nonland permanent you control becomes a copy of the target")
    void copiesControlledNonlandPermanents() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        castMirrorform(target);

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.getCard().getPower()).isEqualTo(3);
        assertThat(bears.getCard().getToughness()).isEqualTo(3);
        assertThat(relic.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(island.getCard().getName()).isEqualTo("Island");
        assertThat(opponentBears.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The copy effect is permanent")
    void copiesDoNotWearOff() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castMirrorform(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("A non-Aura permanent is required as the target")
    void cannotTargetAura() {
        Permanent aura = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        harness.setHand(player1, List.of(new Mirrorform()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
