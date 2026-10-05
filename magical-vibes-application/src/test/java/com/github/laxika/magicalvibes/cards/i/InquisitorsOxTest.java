package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExposeEvil;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorsOx.class, Forest.class, ExposeEvil.class, MagnifyingGlass.class, WickerWitch.class})
class InquisitorsOxTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/5 without delirium")
    void noDeliriumBaseStats() {
        harness.setGraveyard(player1, List.of(new InquisitorsOx(), new Forest(), new ExposeEvil()));
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
                new InquisitorsOx(), new Forest(), new ExposeEvil(), new MagnifyingGlass()));
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

        harness.setGraveyard(player1, List.of(new InquisitorsOx(), new Forest(), new ExposeEvil()));

        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Four cards with only three distinct types do not enable delirium")
    void duplicateCardTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new InquisitorsOx(), new InquisitorsOx(), new Forest(), new ExposeEvil()));
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature contributes both its card types to delirium")
    void multipleTypesOnOneCardCountSeparately() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new Forest(), new ExposeEvil()));
        harness.addToBattlefield(player1, new InquisitorsOx());

        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ox)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Gains delirium immediately when its controller's graveyard reaches four types")
    void gainsDeliriumWhileOnBattlefield() {
        harness.setGraveyard(player1, List.of(new InquisitorsOx(), new Forest(), new ExposeEvil()));
        harness.addToBattlefield(player1, new InquisitorsOx());
        Permanent ox = findOx();
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isFalse();

        setDelirium();

        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ox, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with delirium does not tap the Ox")
    void vigilancePreventsTappingToAttack() {
        setDelirium();
        Permanent ox = addCreatureReady(player1, new InquisitorsOx());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(ox.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking without delirium taps the Ox")
    void attacksTapWithoutDelirium() {
        Permanent ox = addCreatureReady(player1, new InquisitorsOx());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(ox.isTapped()).isTrue();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new InquisitorsOx(), new Forest(), new ExposeEvil(), new MagnifyingGlass()));
    }

    private Permanent findOx() {
        return findPermanent(player1, "Inquisitor's Ox");
    }
}
