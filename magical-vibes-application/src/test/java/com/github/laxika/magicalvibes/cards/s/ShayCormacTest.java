package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.b.BountyHunter;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Progenitus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShayCormac.class, BountyHunter.class, GrizzlyBears.class, Assassinate.class,
        CarnageTyrant.class, DarksteelMyr.class, Progenitus.class})
class ShayCormacTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a bounty counter on an opponent's creature targeted by your ability")
    void marksTargetedOpponentCreature() {
        addCreatureReady(player1, new ShayCormac());
        addCreatureReady(player1, new BountyHunter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.BOUNTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on Shay when a creature with a bounty counter dies")
    void growsWhenBountyCreatureDies() {
        Permanent shay = addCreatureReady(player1, new ShayCormac());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.BOUNTY, 1);
        target.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, java.util.List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(shay.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes opposing keywords and all protection until end of turn")
    void removesOpposingProtectionAndKeywordsUntilEndOfTurn() {
        addCreatureReady(player1, new ShayCormac());
        Permanent ownHexproof = addCreatureReady(player1, new CarnageTyrant());
        Permanent opponentHexproof = addCreatureReady(player2, new CarnageTyrant());
        Permanent opponentIndestructible = addCreatureReady(player2, new DarksteelMyr());
        Permanent opponentProtection = addCreatureReady(player2, new Progenitus());
        Permanent shay = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, opponentProtection, shay)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, opponentProtection, shay)).isTrue();
    }
}
