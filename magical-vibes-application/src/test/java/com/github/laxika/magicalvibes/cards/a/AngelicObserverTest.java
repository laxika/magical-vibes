package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicObserver.class, GallantCitizen.class, GrizzlyBears.class, CivilServant.class})
class AngelicObserverTest extends BaseCardTest {

    @Test
    void paysFullCostWithoutCitizens() {
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Observer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void tappedCitizensEachReduceCostByOne() {
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new CivilServant()).setTapped(true);
        }
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Observer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessCitizensDoNotReduceWhiteManaRequirement() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new CivilServant());
        }
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Observer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Affinity for Citizens reduces the generic mana cost")
    void affinityForCitizensReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GallantCitizen());
        }
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Citizens controlled by the spell's controller")
    void affinityCountsOnlyControlledCitizens() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new GallantCitizen());
        }
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not count non-Citizen permanents")
    void affinityDoesNotCountNonCitizens() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new AngelicObserver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
