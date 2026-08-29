package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnarlwoodDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 1/1 without delirium")
    void noDelirium() {
        Permanent dryad = addDryad(List.of(new Shock(), new Forest(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 with four card types in its controller's graveyard")
    void delirium() {
        Permanent dryad = addDryad(List.of(
                new GnarlwoodDryad(), new Shock(), new Divination(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard falls below four card types")
    void losesDelirium() {
        Permanent dryad = addDryad(List.of(
                new GnarlwoodDryad(), new Shock(), new Divination(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    private Permanent addDryad(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new GnarlwoodDryad());
    }
}
