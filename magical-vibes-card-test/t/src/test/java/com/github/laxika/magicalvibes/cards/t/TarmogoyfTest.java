package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoundInSilence;
import com.github.laxika.magicalvibes.cards.c.CloudKey;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.j.JudgeUnworthy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tarmogoyf.class, DryadArbor.class, CloudKey.class, JudgeUnworthy.class, Foresee.class,
        Imperiosaur.class, BoundInSilence.class, Ghostfire.class})
class TarmogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Has 0/1 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());

        assertThat(gqs.getEffectivePower(gd, goyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power counts distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setGraveyard(player1, List.of(
                new Imperiosaur(), new DryadArbor(), new JudgeUnworthy(), new CloudKey(), new Foresee()));
        harness.setGraveyard(player2, List.of(new Imperiosaur(), new JudgeUnworthy()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("Power and toughness update as graveyard card types change")
    void updatesWhenGraveyardCardTypesChange() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());

        harness.setGraveyard(player1, List.of(new Imperiosaur()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new JudgeUnworthy(), new JudgeUnworthy()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);
    }

    @Test
    @DisplayName("A card with multiple card types contributes each type")
    void countsAllTypesOnOneCard() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setGraveyard(player1, List.of(new DryadArbor()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Kindred and enchantment are separate card types")
    void countsKindredAndEnchantmentSeparately() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setGraveyard(player2, List.of(new BoundInSilence()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cards outside graveyards do not contribute card types")
    void ignoresCardsOutsideGraveyards() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setHand(player1, List.of(new JudgeUnworthy()));
        harness.setLibrary(player2, List.of(new Foresee()));
        harness.setExile(player2, List.of(new BoundInSilence()));
        harness.addToBattlefield(player2, new DryadArbor());

        assertThat(gqs.getEffectivePower(gd, goyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Characteristic power and toughness work in hand and graveyard")
    void definesPowerAndToughnessOutsideBattlefield() {
        Tarmogoyf goyf = new Tarmogoyf();
        harness.setHand(player1, List.of(goyf));
        harness.setGraveyard(player2, List.of(new BoundInSilence()));

        assertThat(gqs.getEffectiveCardPower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, goyf)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(goyf));

        assertThat(gqs.getEffectiveCardPower(gd, goyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, goyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("A resolving damage spell adds its type before lethal damage is checked")
    void survivesWhenDamageSpellAddsNewCardType() {
        Permanent goyf = addCreatureReady(player2, new Tarmogoyf());
        harness.setGraveyard(player2, List.of(new DryadArbor()));
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, goyf.getId());

        harness.assertOnBattlefield(player2, "Tarmogoyf");
        harness.assertInGraveyard(player1, "Ghostfire");
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("A damage spell does not increase toughness when its type is already present")
    void diesWhenDamageSpellAddsNoNewCardType() {
        Permanent goyf = addCreatureReady(player2, new Tarmogoyf());
        harness.setGraveyard(player2, List.of(new Imperiosaur(), new JudgeUnworthy()));
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, goyf.getId());

        harness.assertNotOnBattlefield(player2, "Tarmogoyf");
        harness.assertInGraveyard(player2, "Tarmogoyf");
        harness.assertInGraveyard(player1, "Ghostfire");
    }
}
