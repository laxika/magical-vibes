package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundryChampion.class, DiscipleOfTheOldWays.class, RapidHybridization.class})
class FoundryChampionTest extends BaseCardTest {

    private void castChampion(UUID targetId) {
        harness.setHand(player1, List.of(new FoundryChampion()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB deals damage to target player equal to the number of creatures the controller controls, counting itself")
    void etbDamagesPlayerForCreatureCount() {
        harness.addToBattlefield(player1, new DiscipleOfTheOldWays());
        harness.addToBattlefield(player1, new DiscipleOfTheOldWays());
        harness.addToBattlefield(player2, new DiscipleOfTheOldWays());
        harness.setLife(player2, 20);

        castChampion(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("ETB deals only 1 damage when the Champion is the controller's only creature")
    void etbDamagesForOneWhenAlone() {
        harness.setLife(player2, 20);

        castChampion(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB damage can kill a targeted creature")
    void etbKillsTargetCreature() {
        harness.addToBattlefield(player1, new DiscipleOfTheOldWays());
        harness.addToBattlefield(player2, new DiscipleOfTheOldWays());

        castChampion(harness.getPermanentId(player2, "Disciple of the Old Ways"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Disciple of the Old Ways");
    }

    @Test
    @DisplayName("ETB counts creatures at resolution, including creatures added after it triggered")
    void etbCountsCreaturesAtResolution() {
        harness.setLife(player2, 20);
        castChampion(player2.getId());
        harness.addToBattlefield(player1, new DiscipleOfTheOldWays());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanDamageController() {
        harness.setLife(player1, 20);
        castChampion(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB still deals damage after Foundry Champion leaves the battlefield")
    void etbResolvesAfterSourceIsDestroyed() {
        harness.setLife(player2, 20);
        castChampion(player2.getId());
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Foundry Champion"));
        harness.assertInGraveyard(player1, "Foundry Champion");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not damage the replacement token when its target leaves the battlefield")
    void etbDoesNotFollowDepartedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DiscipleOfTheOldWays());
        castChampion(target.getId());
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Disciple of the Old Ways");
        Permanent token = findPermanent(player2, "Frog Lizard");
        assertThat(token.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pump on the stack does not affect the token replacing its destroyed source")
    void pumpDoesNotFollowDepartedSource() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FoundryChampion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, champion.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Foundry Champion");
        Permanent token = findPermanent(player1, "Frog Lizard");
        assertThat(token.getPowerModifier()).isZero();
        assertThat(token.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pump abilities can be activated repeatedly while summoning sick and tapped")
    void pumpsAccumulateWithoutTapCosts() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FoundryChampion());
        champion.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(champion.getPowerModifier()).isEqualTo(2);
        assertThat(champion.getToughnessModifier()).isEqualTo(2);
        assertThat(champion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{R} gives Foundry Champion +1/+0 until end of turn")
    void redAbilityBoostsPower() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FoundryChampion());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(1);
        assertThat(champion.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{W} gives Foundry Champion +0/+1 until end of turn")
    void whiteAbilityBoostsToughness() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FoundryChampion());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(0);
        assertThat(champion.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump abilities wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FoundryChampion());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(1);
        assertThat(champion.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(0);
        assertThat(champion.getToughnessModifier()).isEqualTo(0);
    }
}
