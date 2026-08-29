package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PrimalPlasma.class)
class PrimalPlasmaTest extends BaseCardTest {

    private Permanent castAndChoose(String choice) {
        harness.setHand(player1, List.of(new PrimalPlasma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (choice != null) {
            harness.handleListChoice(player1, choice);
        }
        return findPermanent(player1, "Primal Plasma");
    }

    @Test
    void resolvingOffersAllThreeForms() {
        castAndChoose(null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("3/3", "2/2 with flying", "1/6 with defender");
    }

    @Test
    void choosesPlainThreeThree() {
        Permanent plasma = castAndChoose("3/3");

        assertThat(gqs.getEffectivePower(gd, plasma)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, plasma)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.DEFENDER)).isFalse();
    }

    @Test
    void choosesTwoTwoWithFlying() {
        Permanent plasma = castAndChoose("2/2 with flying");

        assertThat(gqs.getEffectivePower(gd, plasma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plasma)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.DEFENDER)).isFalse();

        plasma.resetModifiers();
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.FLYING)).isTrue();
    }

    @Test
    void choosesOneSixWithDefender() {
        Permanent plasma = castAndChoose("1/6 with defender");

        assertThat(gqs.getEffectivePower(gd, plasma)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, plasma)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, plasma, Keyword.DEFENDER)).isTrue();
    }
}
