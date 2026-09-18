package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
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

@CardUsed({MithrilCoat.class, KamahlPitFighter.class, GrizzlyBears.class})
class MithrilCoatTest extends BaseCardTest {

    @Test
    @DisplayName("Mithril Coat enters attached to a target legendary creature you control")
    void entersAttachedToLegendaryCreature() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, kamahl.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent coat = findPermanent(player1, "Mithril Coat");
        assertThat(coat.getAttachedTo()).isEqualTo(kamahl.getId());
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Mithril Coat cannot target a nonlegendary creature on entry")
    void cannotTargetNonlegendaryCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MithrilCoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature you control");
    }

    @Test
    @DisplayName("Equip can move Mithril Coat to a nonlegendary creature you control")
    void equipCanTargetAnyCreatureYouControl() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent coat = harness.addToBattlefieldAndReturn(player1, new MithrilCoat());
        coat.setAttachedTo(kamahl.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(coat.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
