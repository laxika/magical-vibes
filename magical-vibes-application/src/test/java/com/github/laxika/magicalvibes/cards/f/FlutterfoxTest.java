package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flutterfox.class, Spellbook.class, Pacifism.class})
class FlutterfoxTest extends BaseCardTest {

    @Test
    void doesNotHaveFlyingWithoutArtifactOrEnchantment() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }

    @Test
    void hasFlyingWhileControllingAnArtifact() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();
    }

    @Test
    void hasFlyingWhileControllingAnEnchantment() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player1, new Pacifism());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();
    }

    @Test
    void opponentArtifactDoesNotCountAndFlyingReturnsWhenOwnArtifactEnters() {
        Permanent flutterfox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Spellbook"));
        assertThat(gqs.hasKeyword(gd, flutterfox, Keyword.FLYING)).isFalse();
    }
}
