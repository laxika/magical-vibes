package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({RazorvergeThicket.class, Mountain.class, Memnite.class})
class RazorvergeThicketTest extends BaseCardTest {

    @Test
    @DisplayName("Enters untapped when you control zero other lands")
    void entersUntappedWithZeroLands() {
        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control one other land")
    void entersUntappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped when you control three other lands")
    void entersTappedWithThreeLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control many other lands")
    void entersTappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        // Add 3 creatures (not lands)
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        // With zero lands and three creatures, the land enters untapped.
        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        // Give opponent 5 lands
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        // Player1 controls zero other lands, so the land enters untapped.
        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addThicketReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addThicketReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two other nonbasic lands and nonland permanents still allow untapped entry")
    void entersUntappedWithTwoNonbasicLandsAndCreatures() {
        harness.addToBattlefield(player1, new RazorvergeThicket());
        harness.addToBattlefield(player1, new RazorvergeThicket());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
            harness.addToBattlefield(player2, new Mountain());
        }
        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent enteringLand = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(enteringLand.isTapped()).isFalse();
    }

    private void addThicketReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RazorvergeThicket());
        perm.setSummoningSick(false);
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findThicket(Player player) {
        return findPermanent(player, "Razorverge Thicket");
    }
}
