package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrovesBounty;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElusiveOtter.class, GrovesBounty.class, GrizzlyBears.class, Opt.class, PhyrexianWalker.class})
class ElusiveOtterTest extends BaseCardTest {

    @Test
    void adventureDistributesCountersAmongCreaturesYouControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ElusiveOtter card = new ElusiveOtter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 3, Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElusiveOtter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, 1, Map.of(opponentCreature.getId(), 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legal target");
    }

    @Test
    void adventureWithZeroXCanHaveNoTargets() {
        ElusiveOtter card = new ElusiveOtter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        ElusiveOtter card = new ElusiveOtter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elusive Otter");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void prowessBoostsTheOtterWhenCastingANoncreatureSpell() {
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new ElusiveOtter());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(otter.getEffectivePower()).isEqualTo(2);
        assertThat(otter.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotBeBlockedByACreatureWithLessPower() {
        Permanent blocker = addCreatureReady(player2, new PhyrexianWalker());
        Permanent otter = addCreatureReady(player1, new ElusiveOtter());
        otter.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(otter);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }
}
