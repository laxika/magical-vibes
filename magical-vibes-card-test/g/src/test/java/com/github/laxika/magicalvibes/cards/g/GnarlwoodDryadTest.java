package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CertainDeath;
import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnarlwoodDryad.class, GrappleWithThePast.class, CertainDeath.class,
        GeierReachSanitarium.class, FieldCreeper.class, WretchedGryff.class})
class GnarlwoodDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 1/1 without delirium")
    void noDelirium() {
        Permanent dryad = addDryad(List.of(new GrappleWithThePast(), new GeierReachSanitarium(), new CertainDeath()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 with four card types in its controller's graveyard")
    void delirium() {
        Permanent dryad = addDryad(List.of(
                new GnarlwoodDryad(), new GrappleWithThePast(), new CertainDeath(), new GeierReachSanitarium()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard falls below four card types")
    void losesDelirium() {
        Permanent dryad = addDryad(List.of(
                new GnarlwoodDryad(), new GrappleWithThePast(), new CertainDeath(), new GeierReachSanitarium()));
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new GrappleWithThePast(), new CertainDeath(), new GeierReachSanitarium()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains the bonus immediately when a fourth card type enters the graveyard")
    void gainsDelirium() {
        Permanent dryad = addDryad(List.of(new GrappleWithThePast(), new CertainDeath(),
                new GeierReachSanitarium()));
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new GrappleWithThePast(), new CertainDeath(),
                new GeierReachSanitarium(), new GnarlwoodDryad()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four cards with only three distinct types do not enable delirium")
    void duplicateTypesDoNotCountTwice() {
        Permanent dryad = addDryad(List.of(new GrappleWithThePast(), new GrappleWithThePast(),
                new CertainDeath(), new GeierReachSanitarium()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact creature contributes both types to delirium")
    void multipleTypesOnOneCard() {
        Permanent dryad = addDryad(List.of(new FieldCreeper(), new CertainDeath(),
                new GeierReachSanitarium()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
    }

    @Test
    @DisplayName("The opponent's graveyard does not contribute to delirium")
    void ignoresOpponentsGraveyard() {
        Permanent dryad = addDryad(List.of(new GrappleWithThePast(), new CertainDeath(),
                new GeierReachSanitarium()));
        harness.setGraveyard(player2, List.of(new GnarlwoodDryad(), new GrappleWithThePast(),
                new CertainDeath(), new GeierReachSanitarium()));

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Delirium boosts only the Dryad, not other creatures")
    void boostIsSelfOnly() {
        Permanent dryad = addDryad(List.of(new GnarlwoodDryad(), new GrappleWithThePast(),
                new CertainDeath(), new GeierReachSanitarium()));
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WretchedGryff());

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Deathtouch kills a creature with more toughness even without delirium")
    void deathtouchWithoutDelirium() {
        Permanent dryad = addCreatureReady(player1, new GnarlwoodDryad());
        dryad.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WretchedGryff());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Wretched Gryff");
        harness.assertNotOnBattlefield(player2, "Wretched Gryff");
        harness.assertInGraveyard(player1, "Gnarlwood Dryad");
    }

    private Permanent addDryad(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new GnarlwoodDryad());
    }
}
