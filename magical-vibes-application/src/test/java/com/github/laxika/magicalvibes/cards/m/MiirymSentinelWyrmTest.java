package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DromokaTheEternal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiirymSentinelWyrm.class, DromokaTheEternal.class, GrizzlyBears.class})
class MiirymSentinelWyrmTest extends BaseCardTest {

    @Test
    @DisplayName("Copies another nontoken Dragon and removes legendary")
    void copiesAnotherNontokenDragonWithoutLegendary() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.setHand(player1, List.of(new DromokaTheEternal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new DromokaTheEternal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
