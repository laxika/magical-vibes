package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkromasMemorial.class, Imperiosaur.class, MarchOfTheMachines.class})
class AkromasMemorialTest extends BaseCardTest {

    private static final List<Keyword> GRANTED = List.of(
            Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE);

    @Test
    @DisplayName("Creatures you control gain all five keywords")
    void grantsKeywordsToOwnCreatures() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new Imperiosaur());

        Permanent creature = findPermanent(player1, "Imperiosaur");
        for (Keyword keyword : GRANTED) {
            assertThat(gqs.hasKeyword(gd, creature, keyword)).as(keyword.name()).isTrue();
        }
    }

    @Test
    @DisplayName("Creatures you control have protection from black and from red")
    void grantsProtectionFromBlackAndRed() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new Imperiosaur());

        Permanent creature = findPermanent(player1, "Imperiosaur");
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures get nothing")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player2, new Imperiosaur());

        Permanent creature = findPermanent(player2, "Imperiosaur");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Also grants its abilities when the Memorial becomes a creature")
    void grantsAbilitiesToAnimatedMemorial() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent memorial = findPermanent(player1, "Akroma's Memorial");
        assertThat(gqs.isCreature(gd, memorial)).isTrue();
        assertThat(gqs.hasKeyword(gd, memorial, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, memorial, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Grants wear off when the Memorial leaves the battlefield")
    void grantsEndWhenMemorialLeaves() {
        Permanent memorial = harness.addToBattlefieldAndReturn(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new Imperiosaur());

        Permanent creature = findPermanent(player1, "Imperiosaur");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, memorial));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Does not change power or toughness")
    void doesNotBoostPowerToughness() {
        harness.addToBattlefield(player1, new AkromasMemorial());
        harness.addToBattlefield(player1, new Imperiosaur());

        Permanent creature = findPermanent(player1, "Imperiosaur");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }
}
