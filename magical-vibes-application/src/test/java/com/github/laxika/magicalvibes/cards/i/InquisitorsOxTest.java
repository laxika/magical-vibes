package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InquisitorsOxTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/5 without delirium")
    void noDeliriumBaseStats() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ox)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and vigilance with four card types in its controller's graveyard")
    void deliriumBonusAtThreshold() {
        setDelirium();
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ox)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Loses its delirium bonus when the graveyard drops below four card types")
    void losesDeliriumBonusWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findOx() {
        return findPermanent(player1, "Inquisitor's Ox");
    }
}
