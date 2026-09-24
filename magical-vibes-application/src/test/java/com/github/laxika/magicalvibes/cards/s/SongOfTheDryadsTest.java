package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfTheDryads.class, GrizzlyBears.class})
class SongOfTheDryadsTest extends BaseCardTest {

    @Test
    void turnsAnyPermanentIntoAColorlessForestLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardTypes(gd, bears)).containsExactly(CardType.LAND);
        assertThat(gqs.effectiveBasicLandTypes(gd, bears)).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
        assertThat(gqs.isCreature(gd, bears)).isFalse();

        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void removingTheAuraRestoresTheEnchantedPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SongOfTheDryads)
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveCardTypes(gd, bears)).containsExactly(CardType.CREATURE);
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isFalse();
    }
}
