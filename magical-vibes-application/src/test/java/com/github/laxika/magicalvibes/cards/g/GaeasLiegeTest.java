package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaeasLiegeTest extends BaseCardTest {

    // ===== Characteristic-defining P/T =====

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

    // ===== {T}: Target land becomes a Forest =====

    @Test
    @DisplayName("Resolving the ability makes the target land become a Forest (rule 305.7 replacement)")
    void abilityTurnsLandIntoForest() {
        Permanent plains = forestTargetPlains(player1);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, plains);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("A land forested by the ability counts toward Gaea's Liege's own power")
    void forestedLandCountsTowardPower() {
        Permanent liege = addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest()); // keeps Gaea's Liege alive (1/1)
        harness.addToBattlefield(player1, new Plains());
        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(1);

        UUID plainsId = harness.getPermanentId(player1, "Plains");
        harness.activateAbility(player1, 0, null, plainsId);
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
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new GaeasLiege());
        harness.addToBattlefield(player1, new Forest()); // keeps Gaea's Liege alive + a legal land target
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Helpers =====

    /**
     * Adds a ready Gaea's Liege for {@code player} kept alive by one Forest (so it is 1/1, not a
     * 0/0 that would die to state-based actions), plus a Plains, then makes the Plains a Forest.
     */
    private Permanent forestTargetPlains(Player player) {
        addCreatureReady(player, new GaeasLiege());
        harness.addToBattlefield(player, new Forest());
        harness.addToBattlefield(player, new Plains());
        harness.forceActivePlayer(player);
        UUID plainsId = harness.getPermanentId(player, "Plains");

        harness.activateAbility(player, 0, null, plainsId);
        harness.passBothPriorities();

        return gqs.findPermanentById(gd, plainsId);
    }
}
