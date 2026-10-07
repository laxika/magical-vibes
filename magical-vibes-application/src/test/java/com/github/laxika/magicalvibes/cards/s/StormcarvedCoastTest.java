package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StormcarvedCoast.class, Mountain.class, LlanowarElves.class})
class StormcarvedCoastTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control many other lands")
    void entersUntappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }

        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = findCoast(player1);
        assertThat(coast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCoastReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addCoastReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonbasic lands count, and an untapped Coast can produce mana immediately")
    void tappedNonbasicLandsCountAndManaIsAvailableImmediately() {
        harness.addToBattlefieldAndReturn(player1, new StormcarvedCoast()).tap();
        harness.addToBattlefieldAndReturn(player1, new StormcarvedCoast()).tap();
        harness.setHand(player1, List.of(new StormcarvedCoast()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent coast = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThat(coast.isTapped()).isFalse();
        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(coast.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering through an effect still applies the tapped entry condition")
    void enteringThroughEffectWithOneOtherLandEntersTapped() {
        harness.addToBattlefield(player1, new Mountain());

        Permanent coast = harness.enterBattlefieldAndReturn(player1, new StormcarvedCoast());

        assertThat(coast.isTapped()).isTrue();
    }
    private Permanent addCoastReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StormcarvedCoast());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findCoast(Player player) {
        return findPermanent(player, "Stormcarved Coast");
    }
}
