package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravbladeHeavy.class, Spellbook.class})
class GravbladeHeavyTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAnArtifact() {
        harness.addToBattlefield(player1, new GravbladeHeavy());

        Permanent heavy = findPermanent(player1, "Gravblade Heavy");
        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void getsBoostAndDeathtouchWhileControllingAnArtifact() {
        harness.addToBattlefield(player1, new GravbladeHeavy());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent heavy = findPermanent(player1, "Gravblade Heavy");
        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void opponentArtifactDoesNotEnableAbility() {
        harness.addToBattlefield(player1, new GravbladeHeavy());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent heavy = findPermanent(player1, "Gravblade Heavy");
        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void losesBoostAndDeathtouchWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new GravbladeHeavy());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent heavy = findPermanent(player1, "Gravblade Heavy");
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Spellbook"));

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }
}
