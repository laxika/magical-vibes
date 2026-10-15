package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SickleRipper;
import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.s.Scuttlemutt;
import com.github.laxika.magicalvibes.cards.b.BlowflyInfestation;
import com.github.laxika.magicalvibes.cards.i.InkfathomInfiltrator;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdOfCinders.class, SickleRipper.class, CrabappleCohort.class, Scuttlemutt.class,
        BlowflyInfestation.class, InkfathomInfiltrator.class})
class CrowdOfCindersTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a black permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of black permanents you control")
    void ptEqualsBlackPermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new SickleRipper());
        harness.addToBattlefield(player1, new SickleRipper());

        // itself + 2 black creatures = 3
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-black permanents are not counted")
    void nonBlackNotCounted() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new CrabappleCohort());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your black permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player2, new SickleRipper());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when black permanents change")
    void ptUpdatesWhenBlackPermanentsChange() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new SickleRipper());
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sickle Ripper"));
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts black noncreature permanents")
    void countsBlackNoncreaturePermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new BlowflyInfestation());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fear prevents nonblack nonartifact creatures from blocking")
    void fearPreventsNonblackNonartifactBlockers() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        Permanent bears = addCreatureReady(player2, new CrabappleCohort());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                        gd.playerBattlefields.get(player1.getId()).indexOf(crowd)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block")
    void fearAllowsBlackAndArtifactBlockers() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        Permanent blackBlocker = addCreatureReady(player2, new SickleRipper());
        Permanent artifactBlocker = addCreatureReady(player2, new Scuttlemutt());

        declareAttackersAndPrepareBlockers(List.of(0));
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crowd);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker),
                        attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker),
                        attackerIndex)));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A multicolored black permanent is counted once")
    void countsMulticoloredBlackPermanentOnce() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new InkfathomInfiltrator());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature becoming black increases P/T")
    void countsChangedColors() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);

        harness.activateAbility(player1, 1, 1, null, mutt.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Characteristic P/T works in the graveyard without counting the card itself")
    void characteristicPowerToughnessInGraveyard() {
        CrowdOfCinders crowd = new CrowdOfCinders();
        harness.setGraveyard(player1, List.of(crowd));
        assertThat(gqs.getEffectiveCardPower(gd, crowd)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, crowd)).isZero();

        harness.addToBattlefield(player1, new SickleRipper());
        harness.addToBattlefield(player2, new SickleRipper());

        assertThat(gqs.getEffectiveCardPower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, crowd)).isEqualTo(1);
    }
}
