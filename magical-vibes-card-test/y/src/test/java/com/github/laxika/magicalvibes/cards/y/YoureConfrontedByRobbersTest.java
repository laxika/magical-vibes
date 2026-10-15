package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureConfrontedByRobbers.class, GrizzlyBears.class, Island.class})
class YoureConfrontedByRobbersTest extends BaseCardTest {

    @Test
    void stallsUpToThreeTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    void callsForThreeSoldierTokens() {
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
    }

    @Test
    void stallForTimeCannotTargetNoncreaturePermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YoureConfrontedByRobbers()));
        harness.addMana(player1, ManaColor.WHITE, 4);
    }

    @Test
    void stallForTimeCanChooseNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        harness.assertInGraveyard(player1, "You're Confronted by Robbers");
    }

    @Test
    void stallForTimeCanTargetCreaturesOfBothPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 0, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void stallForTimeCannotChooseFourTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stallForTimeStillTapsRemainingTargetWhenAnotherLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void callForAidCreatesUntappedWhiteSoldiersOnlyForCaster() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.SOLDIER);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        assertThat(opposing.isTapped()).isFalse();
    }
}
