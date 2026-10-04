package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeafeningSilence;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flutterfox.class, Gingerbrute.class, DeafeningSilence.class})
class FlutterfoxTest extends BaseCardTest {

    @Test
    void doesNotHaveFlyingWithoutArtifactOrEnchantment() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }

    @Test
    void hasFlyingWhileControllingAnArtifact() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player1, new Gingerbrute());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();
    }

    @Test
    void hasFlyingWhileControllingAnEnchantment() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();
    }

    @Test
    void opponentArtifactDoesNotCountAndFlyingReturnsWhenOwnArtifactEnters() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player2, new Gingerbrute());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new Gingerbrute());
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Gingerbrute"));
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }

    @Test
    void opponentEnchantmentDoesNotGrantFlying() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player2, new DeafeningSilence());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }

    @Test
    void keepsFlyingUntilBothArtifactAndEnchantmentLeave() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DeafeningSilence());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }

    @Test
    void grantsFlyingOnlyToFlutterfox() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isFalse();
    }
}
