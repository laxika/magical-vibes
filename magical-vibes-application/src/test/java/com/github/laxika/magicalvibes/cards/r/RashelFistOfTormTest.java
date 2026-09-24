package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RashelFistOfTorm.class, Pacifism.class, GrizzlyBears.class})
class RashelFistOfTormTest extends BaseCardTest {

    @Test
    @DisplayName("Auras you control have exalted")
    void controlledAuraGrantsExalted() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Auras an opponent controls do not have exalted from Rashel")
    void opponentControlledAuraDoesNotGrantExalted() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addAura(player2, enchanted);

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    private void addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Pacifism());
        aura.setAttachedTo(enchanted.getId());
    }
}
