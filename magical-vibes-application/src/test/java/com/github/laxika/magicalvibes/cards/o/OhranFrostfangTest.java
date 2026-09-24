package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({OhranFrostfang.class, GrizzlyBears.class, Forest.class})
class OhranFrostfangTest extends BaseCardTest {

    @Test
    void attackingCreaturesYouControlHaveDeathtouch() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();

        attacker.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();

        frostfang.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, frostfang, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void drawsWhenAControlledCreatureDealsCombatDamageToAPlayer() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }
    @Test
    @DisplayName("Draws a card when a creature you control deals combat damage to a player")
    void drawsForAllyCombatDamage() {
        harness.addToBattlefield(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        setLibrary(List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

}
