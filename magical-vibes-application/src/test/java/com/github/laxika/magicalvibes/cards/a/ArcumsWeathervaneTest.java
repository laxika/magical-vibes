package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    }

    @Test
    @DisplayName("Snow granted to an opponent's basic land counts for snow-matters checks")
    void worksOnOpponentLands() {
        Permanent island = addLand(player2);

        activate(1, island);

        assertThat(gqs.hasEffectiveSupertype(gd, island, CardSupertype.SNOW)).isTrue();
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
    @DisplayName("Second ability cannot target a creature")
    void secondAbilityRejectsCreature() {
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, weathervane), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(int abilityIndex, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, weathervane), abilityIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addLand(Player player) {
        Permanent land = new Permanent(new Island());
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }

    private Permanent addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Island());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
        return snowLand;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
