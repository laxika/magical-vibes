package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TegwyllDukeOfSplendor.class, WillowFaerie.class, GrizzlyBears.class, Murder.class})
class TegwyllDukeOfSplendorTest extends BaseCardTest {

    @Test
    @DisplayName("Other Faeries you control get +1/+1")
    void buffsOtherFaeriesYouControl() {
        harness.addToBattlefield(player1, new WillowFaerie());
        Permanent faerie = findPermanent(player1, "Willow Faerie");
        int basePower = gqs.getEffectivePower(gd, faerie);
        int baseToughness = gqs.getEffectiveToughness(gd, faerie);

        harness.addToBattlefield(player1, new TegwyllDukeOfSplendor());

        assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The static ability excludes Tegwyll, non-Faeries, and opposing Faeries")
    void staticAbilityExcludesUnqualifiedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WillowFaerie());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingFaerie = findPermanent(player2, "Willow Faerie");
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opposingPower = gqs.getEffectivePower(gd, opposingFaerie);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingFaerie);

        TegwyllDukeOfSplendor card = new TegwyllDukeOfSplendor();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent tegwyll = findPermanent(player1, "Tegwyll, Duke of Splendor");

        assertThat(gqs.getEffectivePower(gd, tegwyll)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, tegwyll)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.getEffectivePower(gd, opposingFaerie)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingFaerie)).isEqualTo(opposingToughness);
    }

    @Test
    @DisplayName("Another Faerie dying draws a card and causes 1 life loss")
    void anotherFaerieDeathDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new TegwyllDukeOfSplendor());
        harness.addToBattlefield(player1, new WillowFaerie());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player1, "Willow Faerie");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Faerie or opposing Faerie dying does not trigger")
    void unrelatedDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new TegwyllDukeOfSplendor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WillowFaerie());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.setHand(player1, List.of(new Murder()));
        killCreature(player1, "Willow Faerie");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player targetController, String name) {
        UUID targetId = harness.getPermanentId(targetController, name);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
