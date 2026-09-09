package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishMechanics.class, Spellbook.class, GrizzlyBears.class})
class OrcishMechanicsTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactAndDealsDamageToTargetPlayer() {
        Permanent mechanics = addCreatureReady(player1, new OrcishMechanics());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mechanics.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void sacrificesAnArtifactAndDealsDamageToTargetCreature() {
        addCreatureReady(player1, new OrcishMechanics());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
