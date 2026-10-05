package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Prickleboar.class})
class PrickleboarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and first strike during its controller's turn")
    void boostedOnControllerTurn() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Is a plain 3/3 without first strike during other players' turns")
    void notBoostedOnOpponentTurn() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonus follows the active player, so only the attacker's boar is pumped")
    void bonusFlipsWithActivePlayer() {
        Permanent ownBoar = harness.addToBattlefieldAndReturn(player1, new Prickleboar());
        Permanent enemyBoar = harness.addToBattlefieldAndReturn(player2, new Prickleboar());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, ownBoar)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownBoar, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enemyBoar)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enemyBoar, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, ownBoar)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyBoar)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking Prickleboar kills a blocking Prickleboar before it can deal damage")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        addCreatureReady(player1, new Prickleboar());
        addCreatureReady(player2, new Prickleboar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Prickleboar");
        harness.assertNotInGraveyard(player1, "Prickleboar");
        harness.assertNotOnBattlefield(player2, "Prickleboar");
        harness.assertInGraveyard(player2, "Prickleboar");
        harness.assertLife(player2, 20);
    }
}
