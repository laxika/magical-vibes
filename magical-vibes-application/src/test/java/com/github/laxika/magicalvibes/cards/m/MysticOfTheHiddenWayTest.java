package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticOfTheHiddenWay.class})
class MysticOfTheHiddenWayTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndBecomesUnblockableWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new MysticOfTheHiddenWay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent mystic = findPermanent(player1, "Mystic of the Hidden Way");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));
        harness.passBothPriorities();

        assertThat(mystic.isFaceDown()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, mystic)).isTrue();
    }

    @Test
    void normallyCastMysticCannotBeBlocked() {
        harness.setHand(player1, List.of(new MysticOfTheHiddenWay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mystic = findPermanent(player1, "Mystic of the Hidden Way");
        mystic.setSummoningSick(false);
        addCreatureReady(player2, new MysticOfTheHiddenWay());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void faceDownMysticCanBeBlocked() {
        harness.setHand(player1, List.of(new MysticOfTheHiddenWay()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent mystic = findPermanent(player1, "Mystic of the Hidden Way");
        mystic.setSummoningSick(false);
        addCreatureReady(player2, new MysticOfTheHiddenWay());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mystic of the Hidden Way");
        harness.assertInGraveyard(player2, "Mystic of the Hidden Way");
    }

    @Test
    void turningFaceUpAfterBlockersDoesNotUndoTheBlock() {
        harness.setHand(player1, List.of(new MysticOfTheHiddenWay()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent mystic = findPermanent(player1, "Mystic of the Hidden Way");
        mystic.setSummoningSick(false);
        addCreatureReady(player2, new MysticOfTheHiddenWay());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.turnFaceUp(player1, 0));
        assertThat(mystic.isFaceDown()).isFalse();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mystic of the Hidden Way");
        harness.assertInGraveyard(player2, "Mystic of the Hidden Way");
    }
}
