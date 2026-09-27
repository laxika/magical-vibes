package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeOfTheParty.class, GrizzlyBears.class})
class LifeOfThePartyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts Life of the Party by the number of creatures you control")
    void attackBoostCountsControlledCreatures() {
        Permanent lifeOfTheParty = addCreatureReady(player1, new LifeOfTheParty());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(lifeOfTheParty.getPowerModifier()).isEqualTo(2);
        assertThat(lifeOfTheParty.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB gives each opponent a goaded token copy")
    void etbCreatesGoadedTokenCopyForOpponent() {
        Permanent lifeOfTheParty = castLifeOfTheParty();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1)
                .first()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(als.getMustAttackRequirementCount(gd, token)).isEqualTo(1);
                });
        assertThat(als.getMustAttackRequirementCount(gd, lifeOfTheParty)).isZero();
    }

    @Test
    @DisplayName("Token copies do not retrigger Life of the Party's ETB")
    void tokenCopyDoesNotRetriggerEtb() {
        castLifeOfTheParty();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private Permanent castLifeOfTheParty() {
        harness.setHand(player1, List.of(new LifeOfTheParty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Life of the Party");
    }
}
