package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderbondVanguard.class, GrizzlyBears.class})
class ThunderbondVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures its controller controls")
    void powerAndToughnessCountControlledCreatures() {
        Permanent vanguard = addCreatureReady(player1, new ThunderbondVanguard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature token enters as a copy of Thunderbond Vanguard")
    void tokenEntersAsCopy() {
        addCreatureReady(player1, new ThunderbondVanguard());

        Permanent token = harness.enterBattlefieldAndReturn(player1, tokenCreature("Soldier"));

        assertThat(token.getCard().getName()).isEqualTo("Thunderbond Vanguard");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures and opponent tokens are not copied")
    void onlyOwnTokensAreCopied() {
        addCreatureReady(player1, new ThunderbondVanguard());

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentToken = harness.enterBattlefieldAndReturn(player2, tokenCreature("Soldier"));

        assertThat(creature.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(opponentToken.getCard().getName()).isEqualTo("Soldier");
    }

    private Card tokenCreature(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
