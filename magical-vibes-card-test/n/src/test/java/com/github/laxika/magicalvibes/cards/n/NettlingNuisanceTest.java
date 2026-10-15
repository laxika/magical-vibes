package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NettlingNuisance.class, GrizzlyBears.class})
class NettlingNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("One or more Faeries dealing combat damage gives the damaged player one goaded Pirate")
    void faeriesCreateOneGoadedPirateForDamagedPlayer() {
        Permanent nettlingNuisance = addCreatureReady(player1, new NettlingNuisance());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        Permanent pirate = findPermanent(player2, "Pirate");
        assertThat(countPermanents(player2, "Pirate")).isOne();
        assertThat(gqs.isGoaded(gd, pirate)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, pirate)).isOne();
        assertThat(bls.canBlockAttacker(gd, pirate, nettlingNuisance,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Combat damage from a non-Faerie does not create a Pirate")
    void nonFaerieDoesNotTrigger() {
        addCreatureReady(player1, new NettlingNuisance());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Pirate")).isZero();
    }
}
