package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThrabenFoulbloodsTest extends BaseCardTest {

    @Test
    @DisplayName("Has no delirium bonus with fewer than four card types in its controller's graveyard")
    void noDeliriumBonus() {
        Permanent foulbloods = addFoulbloods();

        assertThat(gqs.getEffectivePower(gd, foulbloods)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, foulbloods)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, foulbloods, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and menace with four card types in its controller's graveyard")
    void deliriumBonus() {
        harness.setGraveyard(player1, graveyardWithFourCardTypes());
        Permanent foulbloods = addFoulbloods();

        assertThat(gqs.getEffectivePower(gd, foulbloods)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, foulbloods)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, foulbloods, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Loses the delirium bonus when its controller's graveyard drops below four card types")
    void losesDeliriumWhenGraveyardChanges() {
        harness.setGraveyard(player1, graveyardWithFourCardTypes());
        Permanent foulbloods = addFoulbloods();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, foulbloods)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, foulbloods)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, foulbloods, Keyword.MENACE)).isFalse();
    }

    private Permanent addFoulbloods() {
        return harness.addToBattlefieldAndReturn(player1, new ThrabenFoulbloods());
    }

    private List<Card> graveyardWithFourCardTypes() {
        return List.of(new GrizzlyBears(), new Plains(), new Shock(), new Millstone());
    }
}
