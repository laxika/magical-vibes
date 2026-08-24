package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellrunePainter.class, SpellruneHowler.class, Shock.class, Ponder.class, GrizzlyBears.class})
class SpellrunePainterTest extends BaseCardTest {

    @Test
    void painterGetsPlusOnePlusOneForInstantOrSorcery() {
        Permanent painter = addPainter();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(painter.getPowerModifier()).isEqualTo(1);
        assertThat(painter.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void painterDoesNotTriggerForCreatureSpell() {
        Permanent painter = addPainter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(painter.getPowerModifier()).isZero();
        assertThat(painter.getToughnessModifier()).isZero();
    }

    @Test
    void howlerGetsPlusTwoPlusTwoAfterBecomingNight() {
        gd.dayNight = DayNight.DAY;
        Permanent painter = addPainter();

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(painter.isTransformed()).isTrue();
        assertThat(painter.getCard()).isInstanceOf(SpellruneHowler.class);

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(painter.getPowerModifier()).isEqualTo(2);
        assertThat(painter.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addPainter() {
        harness.addToBattlefield(player1, new SpellrunePainter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
