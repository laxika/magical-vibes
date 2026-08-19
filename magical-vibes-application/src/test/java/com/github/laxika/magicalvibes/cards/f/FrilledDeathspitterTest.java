package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FrilledDeathspitterTest extends BaseCardTest {

    @Test
    void spellDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID deathspitterId = harness.getPermanentId(player2, "Frilled Deathspitter");
        harness.castInstant(player1, 0, deathspitterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
    }
}
