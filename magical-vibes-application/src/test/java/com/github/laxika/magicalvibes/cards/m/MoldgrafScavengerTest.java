package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JacesScrutiny;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoldgrafScavenger.class, Forest.class, JacesScrutiny.class, WickerWitch.class})
class MoldgrafScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the delirium bonus with fewer than four card types")
    void noDelirium() {
        harness.setGraveyard(player1, List.of(new MoldgrafScavenger(), new Forest(), new JacesScrutiny()));
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +3/+0 with four card types in its controller's graveyard")
    void deliriumBonusAtThreshold() {
        setDelirium();
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new MoldgrafScavenger(), new Forest(), new JacesScrutiny(), new WickerWitch()));
        harness.addToBattlefield(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, findScavenger())).isEqualTo(0);
    }

    @Test
    @DisplayName("Loses the delirium bonus when its controller's graveyard drops below four card types")
    void losesDeliriumBonusWhenGraveyardChanges() {
        setDelirium();
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new MoldgrafScavenger());
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new MoldgrafScavenger(), new Forest(), new JacesScrutiny()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gains delirium immediately when a fourth card type reaches the graveyard")
    void gainsDeliriumWhileOnBattlefield() {
        harness.setGraveyard(player1, List.of(new MoldgrafScavenger(), new Forest(), new JacesScrutiny()));
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new MoldgrafScavenger());
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(0);

        setDelirium();

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not enable delirium")
    void duplicateCardTypesDoNotCountTwice() {
        harness.setGraveyard(player1, List.of(
                new MoldgrafScavenger(), new MoldgrafScavenger(), new Forest(), new JacesScrutiny()));
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new MoldgrafScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(0);
    }

    @Test
    @DisplayName("An artifact creature contributes both card types, enabling delirium with three cards")
    void multipleTypesOnOneCardCountSeparately() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new Forest(), new JacesScrutiny()));
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new MoldgrafScavenger());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new WickerWitch());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new MoldgrafScavenger(), new Forest(), new JacesScrutiny(), new WickerWitch()));
    }

    private Permanent findScavenger() {
        return findPermanent(player1, "Moldgraf Scavenger");
    }
}
