package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SetonKrosanProtector.class, AvenFisher.class, DruidLyrist.class})
class SetonKrosanProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Druid adds green mana")
    void tapsDruidForGreenMana() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only an untapped Druid the controller controls can be tapped")
    void ignoresNonDruidsAndOpponents() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        Permanent nonDruid = addCreatureReady(player1, new AvenFisher());
        Permanent opponentSeton = addCreatureReady(player2, new SetonKrosanProtector());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(nonDruid.isTapped()).isFalse();
        assertThat(opponentSeton.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another untapped Druid you control can pay the cost")
    void anotherDruidCanPayTheCost() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        seton.tap();
        Permanent otherDruid = addCreatureReady(player1, new DruidLyrist());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(otherDruid.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without an untapped Druid")
    void cannotActivateWithoutUntappedDruid() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        seton.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Seton can tap itself despite summoning sickness")
    void summoningSickSetonCanTapItself() {
        Permanent seton = harness.addToBattlefieldAndReturn(player1, new SetonKrosanProtector());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Seton can tap a summoning sick Druid")
    void summoningSickDruidCanPayTheCost() {
        Permanent seton = harness.addToBattlefieldAndReturn(player1, new SetonKrosanProtector());
        seton.tap();
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidLyrist());

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses which Druid to tap and can activate again")
    void choosesDruidAndActivatesAgain() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        Permanent druid = addCreatureReady(player1, new DruidLyrist());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, druid.getId());

        assertThat(druid.isTapped()).isTrue();
        assertThat(seton.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-Druids and opposing Druids cannot pay when Seton is tapped")
    void cannotPayWithNonDruidOrOpposingDruid() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        seton.tap();
        Permanent nonDruid = addCreatureReady(player1, new AvenFisher());
        Permanent opposingDruid = addCreatureReady(player2, new DruidLyrist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(nonDruid.isTapped()).isFalse();
        assertThat(opposingDruid.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }
}
