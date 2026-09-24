package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwestruckCygnet.class, CloudSprite.class})
class AwestruckCygnetTest extends BaseCardTest {

    @Test
    @DisplayName("Starts as a 2/1 without flying, vigilance, or the transformed name")
    void startsUntransformed() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        assertThat(gqs.getEffectivePower(gd, cygnet)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cygnet)).isEqualTo(1);
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Awestruck Cygnet");
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Three other flying creatures transform all owned copies")
    void threeFlyingCreaturesTransformOwnedCopies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        castCloudSprite(player1);

        assertThat(gd.getCardIntensity(first.getCard().getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(second.getCard().getId())).isEqualTo(1);
        gd.intensifyCard(first.getCard(), 2);
        gd.intensifyCard(second.getCard(), 2);

        assertThat(gd.getCardIntensity(first.getCard().getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(second.getCard().getId())).isEqualTo(3);
        assertTransformed(first);
        assertTransformed(second);
    }

    @Test
    @DisplayName("Does not intensify an opponent's copy")
    void doesNotIntensifyOpponentsCopy() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AwestruckCygnet());

        castCloudSprite(player1);

        assertThat(gd.getCardIntensity(own.getCard().getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponent.getCard().getId())).isZero();
    }

    private void assertTransformed(Permanent cygnet) {
        assertThat(gqs.getEffectivePower(gd, cygnet)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cygnet)).isEqualTo(4);
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Radiant Swan");
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.VIGILANCE)).isTrue();
    }

    private void castCloudSprite(Player player) {
        harness.setHand(player, List.of(new CloudSprite()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
