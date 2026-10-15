package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamrootCascade.class, Mountain.class, DelugeVirtuoso.class})
class DreamrootCascadeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control many other lands")
    void entersUntappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DelugeVirtuoso());
        }

        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent cascade = findCascade(player1);
        assertThat(cascade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addCreatureReady(player1, new DreamrootCascade());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCreatureReady(player1, new DreamrootCascade());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped basic and nonbasic lands both count toward the land check")
    void tappedBasicAndNonbasicLandsCount() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.addToBattlefieldAndReturn(player1, new DreamrootCascade()).tap();
        harness.setHand(player1, List.of(new DreamrootCascade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanents(player1, "Dreamroot Cascade"))
                .hasSize(2)
                .anySatisfy(cascade -> assertThat(cascade.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Entering from another zone also checks other lands")
    void enteringWithoutLandPlayChecksOtherLands() {
        addBasicLand(player1);

        Permanent cascade = harness.enterBattlefieldAndReturn(player1, new DreamrootCascade());

        assertThat(cascade.isTapped()).isTrue();
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }
    private Permanent findCascade(Player player) {
        return findPermanent(player, "Dreamroot Cascade");
    }
}
