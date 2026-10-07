package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CabarettiInitiate;
import com.github.laxika.magicalvibes.cards.c.CutOfTheProfits;
import com.github.laxika.magicalvibes.cards.d.DisdainfulStroke;
import com.github.laxika.magicalvibes.cards.g.GirderGoons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicateInfiltrator.class, Forest.class, GrizzlyBears.class, HillGiant.class, Murder.class, Shock.class,
        CabarettiInitiate.class, CutOfTheProfits.class, DisdainfulStroke.class, GirderGoons.class})
class SyndicateInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with five distinct graveyard mana values")
    void gainsBoostAtThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new HillGiant()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get the graveyard bonus with fewer than five distinct mana values")
    void doesNotGainBonusBelowThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new HillGiant(), new Murder(), new GrizzlyBears()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus updates as distinct mana values enter and leave the graveyard")
    void bonusTracksGraveyardChanges() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());
        harness.setGraveyard(player1, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new Murder()));

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new Murder(), new SyndicateInfiltrator()));

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(
                new CabarettiInitiate(), new DisdainfulStroke(), new Murder(), new SyndicateInfiltrator()));

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's graveyard does not contribute mana values")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new Murder()));
        harness.setGraveyard(player2, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new Murder(), new SyndicateInfiltrator()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("X in a graveyard card's mana cost does not add a distinct mana value")
    void xCardSharesItsPrintedNonXManaValue() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new CutOfTheProfits(), new Murder()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(3);
    }

    @Test
    @DisplayName("More than five distinct mana values grants only +2/+2 to the source")
    void bonusDoesNotScaleOrAffectOtherCreatures() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new CabarettiInitiate(), new DisdainfulStroke(), new Murder(),
                new SyndicateInfiltrator(), new GirderGoons()));
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SyndicateInfiltrator());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new CabarettiInitiate());

        assertThat(gqs.getEffectivePower(gd, infiltrator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, infiltrator)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }
}
