package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HawkeaterMoth.class, HeatRay.class, WizardMentor.class})
class HawkeaterMothTest extends BaseCardTest {

    @Test
    @DisplayName("Hawkeater Moth has flying and shroud")
    void hasFlyingAndShroud() {
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new HawkeaterMoth());

        assertThat(gqs.hasKeyword(gd, moth, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, moth, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Hawkeater Moth cannot be targeted by a spell")
    void cannotBeTargetedBySpell() {
        harness.addToBattlefield(player1, new HawkeaterMoth());
        Permanent moth = findPermanent(player1, "Hawkeater Moth");
        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Hawkeater Moth cannot be targeted by an activated ability")
    void cannotBeTargetedByAbility() {
        addCreatureReady(player1, new WizardMentor());
        Permanent moth = addCreatureReady(player1, new HawkeaterMoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
