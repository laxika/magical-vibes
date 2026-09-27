package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AislingLeprechaun.class, KoboldsOfKherKeep.class})
class AislingLeprechaunTest extends BaseCardTest {

    @Test
    @DisplayName("A creature blocked by Aisling Leprechaun becomes green indefinitely")
    void creatureBlockedByLeprechaunBecomesGreenIndefinitely() {
        Permanent attacker = addCreatureReady(player1, new KoboldsOfKherKeep());
        addCreatureReady(player2, new AislingLeprechaun());

        declareAttackersAndPrepareBlockers(List.of(0));
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
        addCreatureReady(player1, new AislingLeprechaun());
        Permanent blocker = addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, blocker)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Aisling Leprechaun turns each creature blocking it green")
    void everyBlockerBecomesGreen() {
        addCreatureReady(player1, new AislingLeprechaun());
        Permanent firstBlocker = addCreatureReady(player2, new KoboldsOfKherKeep());
        Permanent secondBlocker = addCreatureReady(player2, new KoboldsOfKherKeep());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectiveColors(gd, firstBlocker)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, secondBlocker)).containsExactly(CardColor.GREEN);
    }
}
