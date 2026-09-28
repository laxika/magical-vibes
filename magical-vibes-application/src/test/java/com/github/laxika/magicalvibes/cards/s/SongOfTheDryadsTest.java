package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfTheDryads.class, GrizzlyBears.class})
class SongOfTheDryadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted permanent becomes a colorless Forest land")
    void enchantedPermanentBecomesColorlessForest() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Enchanted permanent gains the Forest mana ability")
    void enchantedPermanentProducesGreenMana() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new SongOfTheDryads());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, bears)).doesNotContain(CardColor.GREEN);
    }

    @Test
    @DisplayName("Removing Song of the Dryads restores the enchanted permanent")
    void removingAuraRestoresPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new SongOfTheDryads());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isLand(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN);
    }
}
