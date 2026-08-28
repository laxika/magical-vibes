package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinTombRaider.class, GolemsHeart.class})
class GoblinTombRaiderTest extends BaseCardTest {

    @Test
    void getsPowerAndHasteWhileControllingAnArtifact() {
        harness.addToBattlefield(player1, new GoblinTombRaider());
        harness.addToBattlefield(player1, new GolemsHeart());

        Permanent raider = findPermanent(player1, "Goblin Tomb Raider");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotGetBonusWithoutAnArtifact() {
        harness.addToBattlefield(player1, new GoblinTombRaider());

        Permanent raider = findPermanent(player1, "Goblin Tomb Raider");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new GoblinTombRaider());
        harness.addToBattlefield(player2, new GolemsHeart());

        Permanent raider = findPermanent(player1, "Goblin Tomb Raider");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesBonusWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new GoblinTombRaider());
        harness.addToBattlefield(player1, new GolemsHeart());

        Permanent raider = findPermanent(player1, "Goblin Tomb Raider");
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof GolemsHeart);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }
}
