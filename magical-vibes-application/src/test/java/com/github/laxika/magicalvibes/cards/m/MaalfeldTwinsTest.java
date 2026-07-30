package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaalfeldTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("When Maalfeld Twins dies, two 2/2 Zombie tokens are created")
    void deathTriggerCreatesTwoZombies() {
        harness.addToBattlefield(player1, new MaalfeldTwins());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves, Maalfeld Twins dies

        harness.assertInGraveyard(player1, "Maalfeld Twins");
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // death trigger resolves

        List<Permanent> tokens = findPermanents(player1, "Zombie");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Maalfeld Twins on the battlefield creates no tokens")
    void noTokensWhileAlive() {
        harness.addToBattlefield(player1, new MaalfeldTwins());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}
