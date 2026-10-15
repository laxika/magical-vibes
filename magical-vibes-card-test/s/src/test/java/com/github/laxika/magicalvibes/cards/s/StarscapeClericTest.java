package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LoneMissionary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarscapeCleric.class, LoneMissionary.class})
class StarscapeClericTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new StarscapeCleric()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void gainingLifeMakesEachOpponentLoseOneLife() {
        harness.addToBattlefield(player1, new StarscapeCleric());
        harness.setHand(player1, List.of(new LoneMissionary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotBlock() {
        Permanent cleric = addCreatureReady(player2, new StarscapeCleric());

        assertThat(bls.canBlock(gd, cleric)).isFalse();
    }

    @Test
    void offspringDoesNotCreateTokenWhenUnpaid() {
        harness.setHand(player1, List.of(new StarscapeCleric()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void offspringCopiesLifeGainTriggerAndCannotBlockWithoutCreatingMoreTokens() {
        harness.setHand(player1, List.of(new StarscapeCleric()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(bls.canBlock(gd, token)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void separateLifeGainEventsEachTriggerOnce() {
        harness.addToBattlefield(player1, new StarscapeCleric());

        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(28);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentsLifeGainDoesNotTriggerCleric() {
        harness.addToBattlefield(player1, new StarscapeCleric());

        harness.enterBattlefieldAndReturn(player2, new LoneMissionary());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(24);
    }

    @Test
    void offspringStillCreatesCopyAfterOriginalDies() {
        harness.setHand(player1, List.of(new StarscapeCleric()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent original = gd.playerBattlefields.get(player1.getId()).getFirst();
        original.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void lifeGainTriggerStillResolvesAfterClericDies() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new StarscapeCleric());
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        cleric.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cleric);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
