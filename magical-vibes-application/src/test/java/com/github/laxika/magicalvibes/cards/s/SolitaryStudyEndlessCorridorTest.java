package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolitaryStudyEndlessCorridor.class, GrizzlyBears.class})
class SolitaryStudyEndlessCorridorTest extends BaseCardTest {

    @Test
    void solitaryStudyBoostsCreaturesYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRoom(0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void endlessCorridorConjuresADuplicateAndGivesAControlledCreatureFirstStrike() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(SolitaryStudyEndlessCorridor.class::isInstance);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void endlessCorridorCannotTargetAnOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castingEndlessCorridorConjuresBeforeChoosingTheReflexiveTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoom(1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(SolitaryStudyEndlessCorridor.class::isInstance);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void castingEndlessCorridorDoesNotEnableLockedSolitaryStudy() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoom(1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void roomEnteringWithoutBeingCastHasNoActiveBoostOrUnlockTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new SolitaryStudyEndlessCorridor());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void firstStrikeExpiresAfterTheTurnWhileTheDuplicateRemainsInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoom(1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(SolitaryStudyEndlessCorridor.class::isInstance);
    }

    @Test
    void endlessCorridorConjuresEvenWithoutACreatureToTarget() {
        castRoom(1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(SolitaryStudyEndlessCorridor.class::isInstance);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new SolitaryStudyEndlessCorridor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
    }
}
