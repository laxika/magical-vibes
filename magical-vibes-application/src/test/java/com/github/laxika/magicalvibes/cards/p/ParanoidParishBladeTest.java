package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParanoidParishBlade.class, GrizzlyBears.class, Forest.class, Shock.class,
        Millstone.class, WickerWitch.class})
class ParanoidParishBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 without delirium")
    void noDeliriumBaseStats() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and first strike with four card types in its controller's graveyard")
    void deliriumBonusAtThreshold() {
        setDelirium();
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses its delirium bonus when the graveyard drops below four card types")
    void losesDeliriumBonusWhenGraveyardChanges() {
        setDelirium();
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Four cards with only three card types do not enable delirium")
    void repeatedTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Shock()));
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature contributes both types toward delirium")
    void multipleTypesOnOneCardCountSeparately() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new Forest(), new Shock()));
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Gains delirium immediately when the graveyard reaches four card types")
    void gainsDeliriumAfterEntering() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();

        setDelirium();

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Delirium grants its bonus only to Parish-Blade")
    void deliriumDoesNotBoostOtherCreatures() {
        setDelirium();
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new ParanoidParishBlade());
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new WickerWitch());
        Permanent opposingBlade = harness.addToBattlefieldAndReturn(player2, new ParanoidParishBlade());

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, witch)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, witch, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingBlade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingBlade, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

}
