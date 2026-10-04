package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChromeCompanion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravbladeHeavy.class, ChromeCompanion.class})
class GravbladeHeavyTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAnArtifact() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void getsBoostAndDeathtouchWhileControllingAnArtifact() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        harness.addToBattlefield(player1, new ChromeCompanion());

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void opponentArtifactDoesNotEnableAbility() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        harness.addToBattlefield(player2, new ChromeCompanion());

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void losesBoostAndDeathtouchWhenArtifactLeaves() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        harness.addToBattlefield(player1, new ChromeCompanion());

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Chrome Companion"));

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void gainsAbilityImmediatelyWhenAnArtifactArrives() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new ChromeCompanion());

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void multipleArtifactsGiveOnlyOneBoostAndOneRemainingArtifactKeepsItActive() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        harness.addToBattlefield(player1, new ChromeCompanion());

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void artifactCardsInHandAndGraveyardDoNotEnableAbility() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new GravbladeHeavy());
        harness.setHand(player1, List.of(new ChromeCompanion()));
        harness.setGraveyard(player1, List.of(new ChromeCompanion()));

        assertThat(gqs.getEffectivePower(gd, heavy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heavy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, heavy, Keyword.DEATHTOUCH)).isFalse();
    }
}
