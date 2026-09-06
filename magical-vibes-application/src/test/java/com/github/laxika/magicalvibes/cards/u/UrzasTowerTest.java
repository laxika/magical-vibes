package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.s.SpreadingSeas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasTower.class, UrzasMine.class, UrzasPowerPlant.class})
class UrzasTowerTest extends BaseCardTest {

    @Test
    @CardUsed(SpreadingSeas.class)
    void turningMineIntoIslandRemovesTheManaBonus() {
        harness.addToBattlefield(player1, new UrzasTower());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());
        harness.setHand(player1, List.of(new SpreadingSeas()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, mine.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void renamedLandsStillProvideTheirLandTypes() {
        harness.addToBattlefield(player1, new UrzasTower());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new UrzasMine());
        Permanent plant = harness.addToBattlefieldAndReturn(player1, new UrzasPowerPlant());
        TestCards.mutableCard(mine).setName("Renamed mine");
        TestCards.mutableCard(plant).setName("Renamed plant");

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void matchingNamesWithoutRequiredLandTypesDoNotIncreaseMana() {
        harness.addToBattlefield(player1, new UrzasTower());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());
        TestCards.mutableCard(mine).setSubtypes(List.of(CardSubtype.MINE));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void potentialManaIncludesAllThreeLandsBonusOutput() {
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());

        assertThat(harness.getGameActionAvailabilityService()
                .getPotentialManaTotal(gd, player1.getId())).isEqualTo(7);
    }

    @BeforeEach
    void preload5edOracleData() {
        GameTestHarness.cardCatalog().findByCollectorNumber(CardSet.SET_5ED, "429");
    }
    @Test
    @DisplayName("Tapping alone adds one colorless mana")
    void tapAloneAddsOne() {
        harness.addToBattlefield(player1, new UrzasTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping with a Mine and a Power-Plant adds three colorless mana")
    void tapWithFullTronAddsThree() {
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping with only a Mine adds one colorless mana")
    void tapWithPartialTronAddsOne() {
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player1, new UrzasMine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping with only a Power-Plant adds one colorless mana")
    void tapWithOnlyPowerPlantAddsOne() {
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player1, new UrzasPowerPlant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping with a Mine and a Power-Plant controlled by an opponent adds one colorless mana")
    void tapWithOpponentsTronAddsOne() {
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player2, new UrzasMine());
        harness.addToBattlefield(player2, new UrzasPowerPlant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
