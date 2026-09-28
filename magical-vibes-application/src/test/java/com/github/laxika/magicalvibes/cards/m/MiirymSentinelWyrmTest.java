package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.cards.z.ZurgoAndOjutai;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiirymSentinelWyrm.class, YoungRedDragon.class, BatheInGold.class, GrizzlyBears.class,
        ZurgoAndOjutai.class})
class MiirymSentinelWyrmTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Dragon entering creates a nonlegendary token copy")
    void dragonEnteringCreatesNonlegendaryTokenCopy() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.setHand(player1, List.of(new YoungRedDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Young Red Dragon")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Young Red Dragon")))
                .hasSize(1);
    }

    @Test
    @DisplayName("A non-Dragon creature entering does not trigger Miirym")
    void nonDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token copy of a legendary Dragon is not legendary")
    void tokenCopyIsNotLegendary() {
        harness.addToBattlefield(player1, new MiirymSentinelWyrm());
        harness.setHand(player1, List.of(new ZurgoAndOjutai()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zurgo and Ojutai"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.stack).isEmpty();
    }
}
