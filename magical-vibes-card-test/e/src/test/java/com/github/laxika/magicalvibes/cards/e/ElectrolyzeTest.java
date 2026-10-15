package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BatteringWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Electrolyze.class, BatteringWurm.class})
class ElectrolyzeTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToOneTargetAndDrawsACard() {
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInHand(player1, "Battering Wurm");
    }

    @Test
    void dividesDamageAmongTwoTargetsAndDrawsACard() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, Map.of(wurm.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(wurm.getId())
                        && permanent.getMarkedDamage() == 1);
        harness.assertInHand(player1, "Battering Wurm");
    }

    @Test
    void dealsAllTwoDamageToOneCreatureTarget() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(wurm.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(wurm.getId())
                        && permanent.getMarkedDamage() == 2);
        harness.assertInHand(player1, "Battering Wurm");
    }

    @Test
    void requiresAllTwoDamageToBeAssigned() {
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sum to 2");
    }

    @Test
    void eachChosenTargetMustReceivePositiveDamage() {
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player2.getId(), 2, player1.getId(), 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsAndDoesNotRedistributeDamageWhenOneTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(first.getId(), 1, second.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Battering Wurm");
        harness.assertInGraveyard(player1, "Electrolyze");
    }

    @Test
    void doesNotDrawWhenItsOnlyTargetLeaves() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(wurm.getId(), 2));
        gd.playerBattlefields.get(player2.getId()).remove(wurm);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Battering Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Electrolyze");
    }

    @Test
    void doesNotDrawWhenBothTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(first.getId(), 1, second.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Battering Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Electrolyze");
    }
}
