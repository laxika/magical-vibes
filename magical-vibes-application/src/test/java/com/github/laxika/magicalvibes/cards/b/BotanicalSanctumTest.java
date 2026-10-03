package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ServantOfTheConduit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BotanicalSanctum.class, Mountain.class, ServantOfTheConduit.class})
class BotanicalSanctumTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    void entersAccordingToOtherLandCount(int landCount) {
        for (int i = 0; i < landCount; i++) {
            addMountain(player1);
        }

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isEqualTo(landCount > 2);
    }

    @Test
    void tappedLandsStillCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        }

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void canProduceManaImmediatelyButCannotPayTapCostTwice(int abilityIndex) {
        castBotanicalSanctum();

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        ManaColor chosenColor = abilityIndex == 0 ? ManaColor.GREEN : ManaColor.BLUE;
        ManaColor otherColor = abilityIndex == 0 ? ManaColor.BLUE : ManaColor.GREEN;
        assertThat(findSanctum(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(chosenColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotProduceManaWhenItEntersTapped(int abilityIndex) {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);
        castBotanicalSanctum();

        assertThatThrownBy(() -> harness.activateAbility(player1, 3, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void entersUntappedWithTwoOtherLands() {
        addMountain(player1);
        addMountain(player1);

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isTrue();
    }

    @Test
    void nonLandPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new ServantOfTheConduit());
        harness.addToBattlefield(player1, new ServantOfTheConduit());
        harness.addToBattlefield(player1, new ServantOfTheConduit());

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isFalse();
    }

    @Test
    void opponentsLandsDoNotCount() {
        addMountain(player2);
        addMountain(player2);
        addMountain(player2);

        castBotanicalSanctum();

        assertThat(findSanctum(player1).isTapped()).isFalse();
    }

    @Test
    void tappingProducesGreenMana() {
        addReadySanctum(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlueMana() {
        addReadySanctum(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void castBotanicalSanctum() {
        harness.setHand(player1, List.of(new BotanicalSanctum()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySanctum(Player player) {
        return addCreatureReady(player, new BotanicalSanctum());
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findSanctum(Player player) {
        return findPermanent(player, "Botanical Sanctum");
    }
}
