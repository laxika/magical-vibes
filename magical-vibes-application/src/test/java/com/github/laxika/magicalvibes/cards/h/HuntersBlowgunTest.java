package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntersBlowgun.class, GrizzlyBears.class})
class HuntersBlowgunTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndDeathtouchDuringYourTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = addBlowgunReady(player1);
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void equippedCreatureHasReachInsteadOfDeathtouchDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = addBlowgunReady(player1);
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void conditionalKeywordsChangeWhenActivePlayerChanges() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = addBlowgunReady(player1);
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    private Permanent addBlowgunReady(Player player) {
        Permanent perm = new Permanent(new HuntersBlowgun());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
