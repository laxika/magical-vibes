package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyWillIsIrresistible.class, DarksteelIngot.class, Forest.class, GrizzlyBears.class})
class MyWillIsIrresistibleTest extends BaseCardTest {

    @Test
    void controllerChoosesUpToThreeAndTargetedOpponentLeavesOneBehind() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent own = addCreatureReady(player1, new GrizzlyBears());

        resolveScheme();

        PendingInteraction.MultiPermanentChoice controllerChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(controllerChoice.playerId()).isEqualTo(player1.getId());
        assertThat(controllerChoice.maxCount()).isEqualTo(3);
        assertThat(controllerChoice.validIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId()));

        PendingInteraction.MultiPermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player2.getId());
        assertThat(opponentChoice.validIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(first, third, own)
                .doesNotContain(second, land);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(second, land)
                .doesNotContain(first, third);
    }

    @Test
    void controllerMayChooseNoPermanents() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
    }

    private void resolveScheme() {
        MyWillIsIrresistible scheme = new MyWillIsIrresistible();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                player2.getId(),
                (Zone) null));
        harness.passBothPriorities();
    }
}
