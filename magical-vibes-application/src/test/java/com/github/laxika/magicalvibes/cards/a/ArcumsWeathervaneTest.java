package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Melting;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcumsWeathervane.class, Island.class, SnowCoveredIsland.class, BalduvianBears.class,
        AdarkarWastes.class, Melting.class})
class ArcumsWeathervaneTest extends BaseCardTest {

    private Permanent weathervane;

    @BeforeEach
    void setUpBoard() {
        weathervane = addReady(player1, new ArcumsWeathervane());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("First ability makes a snow land no longer snow")
    void removesSnowFromSnowLand() {
        Permanent snowLand = addSnowLand(player1);

        activate(0, snowLand);

        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isFalse();
        assertThat(weathervane.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability makes a nonsnow basic land snow")
    void makesBasicLandSnow() {
        Permanent island = addLand(player1);

        activate(1, island);

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isTrue();
        assertThat(weathervane.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Snow granted to an opponent's basic land counts for snow-matters checks")
    void worksOnOpponentLands() {
        Permanent island = addLand(player2);

        activate(1, island);

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("First ability can remove snow from an opponent's land")
    void removesSnowFromOpponentLand() {
        Permanent snowLand = addSnowLand(player2);

        activate(0, snowLand);

        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("First ability can target a snow nonbasic land")
    void removesSnowFromSnowNonbasicLand() {
        Permanent snowLand = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.SNOW));

        activate(0, snowLand);

        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("Removing snow can be undone by the second ability")
    void snowCanBeRestored() {
        Permanent snowLand = addSnowLand(player1);

        activate(0, snowLand);
        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isFalse();

        weathervane.untap();
        activate(1, snowLand);

        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("First ability cannot target a nonsnow land")
    void firstAbilityRejectsNonsnowLand() {
        Permanent island = addLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a snow land")
    void secondAbilityRejectsSnowLand() {
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 1, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability cannot target a snow creature")
    void firstAbilityRejectsSnowCreature() {
        Permanent bears = addReady(player1, new BalduvianBears());
        TestCards.mutableCard(bears).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a creature")
    void secondAbilityRejectsCreature() {
        Permanent bears = addReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a nonsnow nonbasic land")
    void secondAbilityRejectsNonbasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability fizzles if its target stops being a land before resolution")
    void firstAbilityFizzlesIfTargetStopsBeingLand() {
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, weathervane), 0, null, snowLand.getId());

        var targetCard = TestCards.mutableCard(snowLand);
        targetCard.setType(CardType.CREATURE);
        targetCard.setPower(1);
        targetCard.setToughness(1);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, snowLand, CardSupertype.SNOW)).isTrue();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Second ability fizzles if its target stops being basic before resolution")
    void secondAbilityFizzlesIfTargetStopsBeingBasic() {
        Permanent island = addLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, weathervane), 1, null, island.getId());

        TestCards.mutableCard(island).setSupertypes(EnumSet.noneOf(CardSupertype.class));
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isFalse();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("A later global snow removal overrides an earlier snow grant")
    void laterGlobalSnowRemovalOverridesEarlierGrant() {
        Permanent island = addLand(player1);

        activate(1, island);
        harness.addToBattlefield(player1, new Melting());

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("A later snow grant overrides an earlier global snow removal")
    void laterGrantOverridesEarlierGlobalSnowRemoval() {
        harness.addToBattlefield(player1, new Melting());
        Permanent island = addLand(player1);

        activate(1, island);

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isTrue();
    }

    private void activate(int abilityIndex, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, weathervane), abilityIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }

    private Permanent addSnowLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredIsland());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
