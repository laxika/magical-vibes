package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasLiege.class, Forest.class, Plains.class, GrizzlyBears.class})
class GaeasLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("While not attacking, P/T equals the number of Forests you control")
    void notAttackingCountsControllerForests() {
        Permanent liege = addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(2);
    }

    @Test
    @DisplayName("While attacking, P/T equals the number of Forests the defending player controls")
    void attackingCountsDefendingPlayerForests() {
        Permanent liege = addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        liege.setAttacking(true);
        liege.setAttackTarget(player2.getId());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving the ability makes the target land become a Forest (rule 305.7 replacement)")
    void abilityTurnsLandIntoForest() {
        Permanent plains = forestTargetPlains(player1);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, plains);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, plains)).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.getOverriddenLandManaColor(gd, plains)).isEqualTo(ManaColor.GREEN);
    }

    @Test
    @DisplayName("The ability can target an opponent's land")
    void abilityCanTargetOpponentsLand() {
        addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveBasicLandTypes(gd, plains)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("A land forested by the ability counts toward Gaea's Liege's own power")
    void forestedLandCountsTowardPower() {
        Permanent liege = addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest()); // keeps Gaea's Liege alive (1/1)
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Forest reverts when Gaea's Liege leaves the battlefield")
    void forestRevertsWhenSourceLeaves() {
        Permanent plains = forestTargetPlains(player1);
        assertThat(gqs.computeStaticBonus(gd, plains).landSubtypeOverriding()).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gaea's Liege"));

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, plains);
        assertThat(bonus.landSubtypeOverriding()).isFalse();
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, plains)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest()); // keeps Gaea's Liege alive + a legal land target
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    /**
     * Adds a ready Gaea's Liege for {@code player} kept alive by one Forest (so it is 1/1, not a
     * 0/0 that would die to state-based actions), plus a Plains, then makes the Plains a Forest.
     */
    private Permanent forestTargetPlains(Player player) {
        addCreatureReady(player, new GaeasLiege());
        harness.addToBattlefield(player, new Forest());
        Permanent plains = harness.addToBattlefieldAndReturn(player, new Plains());
        harness.forceActivePlayer(player);

        harness.activateAbility(player, 0, null, plains.getId());
        harness.passBothPriorities();

        return plains;
    }
}
