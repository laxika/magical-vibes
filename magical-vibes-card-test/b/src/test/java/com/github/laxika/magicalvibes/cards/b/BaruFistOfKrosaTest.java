package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YavimayaCradleOfGrowth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaruFistOfKrosa.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class})
class BaruFistOfKrosaTest extends BaseCardTest {

    @Test
    @DisplayName("A Forest boosts green creatures you control and grants them trample until end of turn")
    void forestBoostsGreenCreaturesAndGrantsTrample() {
        Permanent baru = harness.addToBattlefieldAndReturn(player1, new BaruFistOfKrosa());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int hillGiantPower = gqs.getEffectivePower(gd, hillGiant);
        int opponentBearsPower = gqs.getEffectivePower(gd, opponentBears);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baru)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baru)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, baru, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(hillGiantPower);
        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(opponentBearsPower);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baru)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, baru, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Forest also triggers Baru")
    void opponentsForestTriggersBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Forest land does not trigger Baru")
    void nonForestDoesNotTriggerBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Grandeur creates a Wurm whose size equals your land count")
    void grandeurCreatesWurmBasedOnLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new BaruFistOfKrosa()));

        harness.activateAbility(player1, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Wurm");
        assertThat(wurm.getEffectivePower()).isEqualTo(3);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Grandeur counts only your lands and creates a green Wurm")
    void grandeurCountsOnlyYourLandsAndCreatesGreenWurm() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new BaruFistOfKrosa()));

        harness.activateAbility(player1, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> wurms = findPermanents(player1, "Wurm");
        assertThat(wurms).hasSize(1);
        Permanent wurm = wurms.get(0);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, wurm)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, wurm, CardSubtype.WURM)).isTrue();
    }

    @Test
    @CardUsed({BaruFistOfKrosa.class, GrizzlyBears.class, Island.class, YavimayaCradleOfGrowth.class})
    @DisplayName("A land entering as a Forest because of Yavimaya triggers Baru")
    void islandEnteringAsForestTriggersBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.addToBattlefield(player1, new YavimayaCradleOfGrowth());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @CardUsed({BaruFistOfKrosa.class, GrizzlyBears.class, Island.class, YavimayaCradleOfGrowth.class})
    @DisplayName("An opponent's land entering as a Forest because of Yavimaya triggers Baru")
    void opponentsIslandEnteringAsForestTriggersBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.addToBattlefield(player2, new YavimayaCradleOfGrowth());
        harness.setHand(player2, List.of(new Island()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @CardUsed({BaruFistOfKrosa.class, GrizzlyBears.class, YavimayaCradleOfGrowth.class})
    @DisplayName("Yavimaya itself enters as a Forest and triggers Baru")
    void yavimayasOwnEntryTriggersBaru() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new YavimayaCradleOfGrowth()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A creature entering after the Forest trigger resolves receives no bonus")
    void creatureEnteringAfterResolutionIsNotBoosted() {
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Grandeur in response to the Forest trigger creates a Wurm that receives the bonus")
    void grandeurWurmEnteringBeforeResolutionIsBoosted() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new Forest(), new BaruFistOfKrosa()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Baru, Fist of Krosa");
        harness.passBothPriorities();
        Permanent wurm = findPermanent(player1, "Wurm");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grandeur cannot be activated without another Baru in hand")
    void grandeurRequiresAnotherBaruInHand() {
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
    }

    @Test
    @DisplayName("A Grandeur Wurm keeps its original size when another land enters later")
    void grandeurWurmSizeDoesNotTrackLaterLandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new BaruFistOfKrosa(), new Island()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent wurm = findPermanent(player1, "Wurm");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grandeur with no lands creates a zero-toughness Wurm that dies")
    void grandeurWithNoLandsCreatesWurmThatDies() {
        harness.addToBattlefield(player1, new BaruFistOfKrosa());
        harness.setHand(player1, List.of(new BaruFistOfKrosa()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wurm")).isEmpty();
        harness.assertInGraveyard(player1, "Baru, Fist of Krosa");
    }
}
