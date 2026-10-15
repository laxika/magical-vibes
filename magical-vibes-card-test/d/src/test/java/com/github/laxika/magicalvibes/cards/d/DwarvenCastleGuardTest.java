package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenCastleGuard.class, WrathOfGod.class})
class DwarvenCastleGuardTest extends BaseCardTest {

    @Test
    @DisplayName("When Dwarven Castle Guard dies, it creates a 1/1 colorless Hero token")
    void deathTriggerCreatesHeroToken() {
        harness.addToBattlefield(player1, new DwarvenCastleGuard());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Hero");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HERO);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @CardUsed(DwarvenCastleGuard.class)
    @DisplayName("Simultaneous Guard deaths create one Hero for each controller")
    void simultaneousDeathsCreateTokensForTheirControllers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new DwarvenCastleGuard());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);
        opposing.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dwarven Castle Guard")).isZero();
        assertThat(countPermanents(player2, "Dwarven Castle Guard")).isZero();
        assertThat(countPermanents(player1, "Hero")).isEqualTo(2);
        assertThat(countPermanents(player2, "Hero")).isEqualTo(1);
    }

    @Test
    @CardUsed(DwarvenCastleGuard.class)
    @DisplayName("The Hero token does not inherit the Guard's death trigger")
    void heroDeathDoesNotCreateAnotherToken() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        guard.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent hero = findPermanent(player1, "Hero");
        hero.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
