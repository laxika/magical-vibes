package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaalfeldTwins.class, WrathOfGod.class, DeathWind.class, IntoTheVoid.class})
class MaalfeldTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("When Maalfeld Twins dies, two 2/2 Zombie tokens are created")
    void deathTriggerCreatesTwoZombies() {
        harness.addToBattlefield(player1, new MaalfeldTwins());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("A death from zero toughness creates Zombies for the dying creature's controller")
    void zeroToughnessDeathCreatesTokensForOpponent() {
        harness.addToBattlefield(player2, new MaalfeldTwins());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Maalfeld Twins"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Maalfeld Twins");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Returning Maalfeld Twins to hand does not create Zombies")
    void returningToHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new MaalfeldTwins());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(harness.getPermanentId(player1, "Maalfeld Twins")));

        harness.assertInHand(player1, "Maalfeld Twins");
        harness.assertNotInGraveyard(player1, "Maalfeld Twins");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }
}
