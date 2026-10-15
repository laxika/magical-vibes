package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({DeathcapGlade.class, Mountain.class, LlanowarElves.class})
class DeathcapGladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control many other lands")
    void entersUntappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }

        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new DeathcapGlade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent glade = findGlade(player1);
        assertThat(glade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addGladeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addGladeReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonbasic lands count toward the entry condition")
    void tappedNonbasicLandsCount() {
        harness.addToBattlefieldAndReturn(player1, new DeathcapGlade()).tap();
        harness.addToBattlefieldAndReturn(player1, new DeathcapGlade()).tap();

        Permanent enteringGlade = harness.enterBattlefieldAndReturn(player1, new DeathcapGlade());

        assertThat(enteringGlade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering outside a land play still checks the controller's lands")
    void enteringWithOneOwnLandAndOpponentLandsIsTapped() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        Permanent enteringGlade = harness.enterBattlefieldAndReturn(player1, new DeathcapGlade());

        assertThat(enteringGlade.isTapped()).isTrue();
    }

    private void addGladeReady(Player player) {
        harness.addToBattlefieldAndReturn(player, new DeathcapGlade()).setSummoningSick(false);
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findGlade(Player player) {
        return findPermanent(player, "Deathcap Glade");
    }
}
