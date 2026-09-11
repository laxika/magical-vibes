package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thundermare.class, StripedBears.class, MindStone.class})
class ThundermareTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures on both battlefields")
    void etbTapsAllOtherCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new StripedBears());
        Permanent opposingCreature = addCreatureReady(player2, new StripedBears());
        harness.setHand(player1, List.of(new Thundermare()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → enters, ETB trigger on stack
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap Thundermare itself")
    void etbDoesNotTapSelf() {
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Thundermare").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Thundermare to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        harness.setHand(player1, List.of(new Thundermare()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thundermare = findPermanent(player1, "Thundermare");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(thundermare)));

        assertThat(thundermare.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap other noncreature permanents")
    void etbDoesNotTapNoncreatures() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of(new Thundermare()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mindStone.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps another Thundermare but not the one entering")
    void etbExcludesOnlyTheEnteringThundermare() {
        Permanent existingThundermare = addCreatureReady(player1, new Thundermare());
        harness.setHand(player1, List.of(new Thundermare()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thundermarePermanents = findPermanents(player1, "Thundermare");
        assertThat(thundermarePermanents).hasSize(2);
        assertThat(existingThundermare.isTapped()).isTrue();
        assertThat(thundermarePermanents.get(1).isTapped()).isFalse();
    }
}
