package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SlipOutTheBack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfCaelusNin.class, GrizzlyBears.class, SlipOutTheBack.class, DoomBlade.class})
class DiscipleOfCaelusNinTest extends BaseCardTest {

    @Test
    void eachPlayerKeepsUpToFivePermanentsAndTheRestPhaseOut() {
        List<Permanent> ownBears = addBears(player1, 6);
        List<Permanent> opposingBears = addBears(player2, 6);

        harness.setHand(player1, List.of(new DiscipleOfCaelusNin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent disciple = findPermanent(player1, "Disciple of Caelus Nin");
        PendingInteraction.MultiPermanentChoice ownChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(ownChoice.playerId()).isEqualTo(player1.getId());
        assertThat(ownChoice.maxCount()).isEqualTo(5);
        assertThat(ownChoice.validIds()).contains(disciple.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(disciple.getId(), ownBears.get(0).getId(), ownBears.get(1).getId(),
                        ownBears.get(2).getId(), ownBears.get(3).getId()));

        PendingInteraction.MultiPermanentChoice opposingChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(opposingChoice.playerId()).isEqualTo(player2.getId());
        assertThat(opposingChoice.maxCount()).isEqualTo(5);
        harness.handleMultiplePermanentsChosen(player2,
                opposingBears.subList(0, 5).stream().map(Permanent::getId).toList());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(disciple, ownBears.get(0), ownBears.get(1), ownBears.get(2), ownBears.get(3))
                .doesNotContain(ownBears.get(4), ownBears.get(5));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsAll(opposingBears.subList(0, 5))
                .doesNotContain(opposingBears.get(5));
        assertThat(gd.phasedOutPermanents.get(player1.getId()))
                .contains(ownBears.get(4), ownBears.get(5));
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .contains(opposingBears.get(5));
    }

    @Test
    void preventsPhasedOutPermanentsFromPhasingInUntilItLeaves() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfCaelusNin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, disciple.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(creature);
    }

    private List<Permanent> addBears(com.github.laxika.magicalvibes.model.Player player, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> harness.addToBattlefieldAndReturn(player, new GrizzlyBears()))
                .toList();
    }
}
