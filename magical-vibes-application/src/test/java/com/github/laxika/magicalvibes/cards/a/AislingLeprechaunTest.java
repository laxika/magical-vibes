package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AislingLeprechaun.class, HillGiant.class})
class AislingLeprechaunTest extends BaseCardTest {

    @Test
    @DisplayName("A creature blocked by Aisling Leprechaun becomes green indefinitely")
    void creatureBlockedByLeprechaunBecomesGreenIndefinitely() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new AislingLeprechaun());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, attacker)).containsExactly(CardColor.GREEN);

        gd.expireEndOfTurnFloatingEffects();
        attacker.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, attacker)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Aisling Leprechaun's blocker becomes green")
    void leprechaunsBlockerBecomesGreen() {
        addCreatureReady(player1, new AislingLeprechaun()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, blocker)).containsExactly(CardColor.GREEN);
    }
}
