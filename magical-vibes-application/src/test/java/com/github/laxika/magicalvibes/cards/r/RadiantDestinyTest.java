package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures +1/+1")
    void boostsCreaturesOfChosenType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destiny = addReadyDestiny(CardSubtype.BEAR);

        assertThat(destiny.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("With the city's blessing, matching creatures also have vigilance")
    void grantsVigilanceWithCityBlessing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addReadyDestiny(CardSubtype.BEAR);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Without the city's blessing, matching creatures do not have vigilance")
    void doesNotGrantVigilanceWithoutCityBlessing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addReadyDestiny(CardSubtype.BEAR);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures of another type do not receive Radiant Destiny's bonuses")
    void doesNotAffectDifferentType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addReadyDestiny(CardSubtype.ELF);
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Resolving Radiant Destiny prompts for a creature type")
    void resolvingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new RadiantDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private Permanent addReadyDestiny(CardSubtype chosenSubtype) {
        Permanent destiny = harness.addToBattlefieldAndReturn(player1, new RadiantDestiny());
        destiny.setChosenSubtype(chosenSubtype);
        destiny.setSummoningSick(false);
        return destiny;
    }
}
