package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BubbleSnare;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NighthawkScavenger.class, Forest.class, GrizzlyBears.class, LavaSpike.class,
        Millstone.class, Ornithopter.class, Shock.class, StoneworkPackbeast.class,
        BubbleSnare.class, JaceMirrorMage.class})
class NighthawkScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Has 1/3 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts distinct card types in opponents' graveyards")
    void countsDistinctCardTypesInOpponentsGraveyards() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone(), new LavaSpike(),
                new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates when opponents' graveyard card types change")
    void updatesWhenOpponentsGraveyardCardTypesChange() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());

        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock(), new Millstone()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(4);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(1);
    }

    @Test
    void ignoresTypesFoundOnlyInControllersGraveyard() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player1, List.of(
                new StoneworkPackbeast(), new BubbleSnare(), new JaceMirrorMage()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(1);
    }

    @Test
    void countsBothTypesOfOneArtifactCreatureOnlyOnceEach() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player2, List.of(new StoneworkPackbeast()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);

        harness.setGraveyard(player2, List.of(new StoneworkPackbeast(), new StoneworkPackbeast()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(3);
    }

    @Test
    void countsEnchantmentsAndPlaneswalkersButNotSupertypesOrSubtypes() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player2, List.of(
                new StoneworkPackbeast(), new BubbleSnare(), new JaceMirrorMage()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
    }

    @Test
    void characteristicPowerWorksInHandGraveyardAndExile() {
        Card scavenger = new NighthawkScavenger();
        harness.setGraveyard(player2, List.of(new StoneworkPackbeast()));
        harness.setHand(player1, List.of(scavenger));
        assertThat(gqs.getEffectiveCardPower(gd, scavenger)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(scavenger));
        assertThat(gqs.getEffectiveCardPower(gd, scavenger)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(scavenger));
        assertThat(gqs.getEffectiveCardPower(gd, scavenger)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectiveCardPower(gd, scavenger)).isEqualTo(1);
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        addCreatureReady(player2, new StoneworkPackbeast());
        scavenger.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unblockedCombatDamageGainsLifeEqualToCurrentPower() {
        addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player2, List.of(new StoneworkPackbeast()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void deathtouchKillsFlyingBlockerAndBothControllersGainLife() {
        Permanent attacker = addCreatureReady(player1, new NighthawkScavenger());
        addCreatureReady(player2, new NighthawkScavenger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Nighthawk Scavenger");
        harness.assertInGraveyard(player2, "Nighthawk Scavenger");
        harness.assertNotOnBattlefield(player1, "Nighthawk Scavenger");
        harness.assertNotOnBattlefield(player2, "Nighthawk Scavenger");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
    }
}
