package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreymondAvacynsStalwart.class, AvacynsPilgrim.class, GrizzlyBears.class})
class GreymondAvacynsStalwartTest extends BaseCardTest {

    @Test
    void choosesTwoAbilitiesAndDynamicallyBoostsHumansAtFour() {
        Permanent firstHuman = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent secondHuman = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreymondAvacynsStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "First strike");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .doesNotContain("First strike");
        harness.handleListChoice(player1, "Lifelink");

        Permanent greymond = findPermanent(player1, "Greymond, Avacyn's Stalwart");
        assertThat(gqs.hasKeyword(gd, firstHuman, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstHuman, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstHuman, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(greymond.getEffectivePower()).isEqualTo(3);
        assertThat(greymond.getEffectiveToughness()).isEqualTo(4);

        harness.setHand(player1, List.of(new AvacynsPilgrim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstHuman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstHuman)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondHuman)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, greymond)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, greymond)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(2);
    }
}
