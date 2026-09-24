package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenFarseer.class, ScornfulEgotist.class, TempleOfTheFalseGod.class})
class AvenFarseerTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenAnotherCreatureTurnsFaceUp() {
        Permanent farseer = addCreatureReady(player1, new AvenFarseer());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent faceDownEgotist = findPermanent(player1, "Scornful Egotist");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownEgotist));
        resolveAllTriggers();

        assertThat(farseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersWhenItTurnsFaceUp() {
        Permanent farseer = addCreatureReady(player1, new AvenFarseer());
        farseer.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, farseer);
        resolveAllTriggers();

        assertThat(farseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersForAnOpponentsFaceDownNoncreaturePermanent() {
        Permanent farseer = addCreatureReady(player1, new AvenFarseer());
        Permanent faceDownTemple = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        faceDownTemple.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDownTemple);
        resolveAllTriggers();

        assertThat(faceDownTemple.isFaceDown()).isFalse();
        assertThat(farseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
