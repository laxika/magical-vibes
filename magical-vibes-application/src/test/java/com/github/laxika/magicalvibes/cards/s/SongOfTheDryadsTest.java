package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfTheDryads.class, GrizzlyBears.class, Forest.class})
class SongOfTheDryadsTest extends BaseCardTest {

    @Test
    @DisplayName("Song of the Dryads turns any permanent into a colorless Forest land")
    void turnsCreatureIntoColorlessForestLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachSongOfTheDryads(bears);

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, bears)).containsExactly(CardSubtype.FOREST);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Removing Song of the Dryads restores the enchanted permanent")
    void removingAuraRestoresPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachSongOfTheDryads(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN);
    }

    private Permanent attachSongOfTheDryads(Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SongOfTheDryads());
        aura.setAttachedTo(target.getId());
        return aura;
    }
}
