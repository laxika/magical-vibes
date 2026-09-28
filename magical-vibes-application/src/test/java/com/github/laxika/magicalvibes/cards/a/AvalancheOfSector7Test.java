package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CompositeGolem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvalancheOfSector7.class, CompositeGolem.class, Ornithopter.class})
class AvalancheOfSector7Test extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts opponents control and toughness is three")
    void powerCountsOpponentsArtifacts() {
        Permanent avalanche = harness.addToBattlefieldAndReturn(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, avalanche)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avalanche)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent activating an artifact ability deals damage to that opponent")
    void opponentArtifactActivationDealsDamage() {
        harness.addToBattlefield(player1, new AvalancheOfSector7());
        harness.addToBattlefield(player2, new CompositeGolem());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
