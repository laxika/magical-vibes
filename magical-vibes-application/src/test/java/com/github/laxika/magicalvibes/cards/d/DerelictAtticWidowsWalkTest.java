package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLACK;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DerelictAtticWidowsWalk.class, GrizzlyBears.class})
class DerelictAtticWidowsWalkTest extends BaseCardTest {

    @Test
    void derelictAtticDrawsTwoCardsAndLosesTwoLifeWhenUnlocked() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        castRoom(0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void widowsWalkBoostsAndGrantsDeathtouchToAThatAttacksAlone() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void widowsWalkDoesNotTriggerWhenMoreThanOneCreatureAttacks() {
        castRoom(1);
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(2);
        assertThat(firstAttacker.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(secondAttacker.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void widowsWalkBoostAndDeathtouchWearOffAtEndOfTurn() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();
        assertThat(attacker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new DerelictAtticWidowsWalk()));
        harness.addMana(player1, BLACK, doorIndex == 0 ? 3 : 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }

}
