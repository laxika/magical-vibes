package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeadenMyr;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicVoicesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own white and artifact creatures when the condition is met")
    void boostsOwnWhiteAndArtifactCreatures() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new WhiteKnight());
        harness.addToBattlefield(player1, new LeadenMyr());

        Permanent whiteKnight = findPermanent(player1, "White Knight");
        Permanent leadenMyr = findPermanent(player1, "Leaden Myr");

        assertThat(gqs.getEffectivePower(gd, whiteKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whiteKnight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, leadenMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leadenMyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost own creatures while controlling a nonartifact nonwhite creature")
    void conditionTurnsOffForNonartifactNonwhiteCreature() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player1, new WhiteKnight());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent whiteKnight = findPermanent(player1, "White Knight");
        Permanent leadenMyr = findPermanent(player1, "Leaden Myr");
        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.computeStaticBonus(gd, whiteKnight).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, leadenMyr).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, grizzlyBears).power()).isZero();
    }

    @Test
    @DisplayName("Does not boost creatures controlled by an opponent")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new AngelicVoices());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.computeStaticBonus(gd, opponentBears).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opponentBears).toughness()).isZero();
    }
}
