package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DromokaTheEternal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiirymSentinelWyrm.class, DromokaTheEternal.class, GrizzlyBears.class, Unsummon.class})
class MiirymSentinelWyrmTest extends BaseCardTest {

    @Test
    @DisplayName("Copies another nontoken Dragon and removes legendary")
    void copiesAnotherNontokenDragonWithoutLegendary() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.castFromHand(player1, new DromokaTheEternal(), "{3}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow()
                .getCard()
                .getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("The token copy does not retrigger Miirym")
    void tokenCopyDoesNotRetrigger() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.castFromHand(player1, new DromokaTheEternal(), "{3}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("A nontoken non-Dragon does not trigger Miirym")
    void nonDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyItselfWhenItEnters() {
        harness.castFromHand(player1, new MiirymSentinelWyrm(), "{3}{G}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotCopyOpponentsDragon() {
        harness.addToBattlefield(player2, new MiirymSentinelWyrm());
        harness.castFromHand(player1, new MiirymSentinelWyrm(), "{3}{G}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    void copiesDragonThatLeftBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.castFromHand(player1, new DromokaTheEternal(), "{3}{G}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Dromoka, the Eternal"));
        harness.assertInHand(player1, "Dromoka, the Eternal");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Dromoka, the Eternal");
                    assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
                });
        assertThat(gd.stack).isEmpty();
    }
}
