package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.l.LadyOrca;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AislingLeprechaun.class, KoboldsOfKherKeep.class, Boomerang.class, LadyOrca.class})
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

    @Test
    @DisplayName("The block trigger replaces all colors even after the Leprechaun leaves")
    void blockTriggerResolvesAfterLeprechaunLeaves() {
        Permanent attacker = addCreatureReady(player1, new LadyOrca());
        Permanent leprechaun = addCreatureReady(player2, new AislingLeprechaun());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player2, 0, leprechaun.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Aisling Leprechaun");
        harness.assertNotOnBattlefield(player2, "Aisling Leprechaun");
        assertThat(gqs.getEffectiveColors(gd, attacker)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The becomes-blocked trigger replaces all colors even after the Leprechaun leaves")
    void becomesBlockedTriggerResolvesAfterLeprechaunLeaves() {
        Permanent leprechaun = addCreatureReady(player1, new AislingLeprechaun());
        Permanent blocker = addCreatureReady(player2, new LadyOrca());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player1, 0, leprechaun.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Aisling Leprechaun");
        harness.assertNotOnBattlefield(player1, "Aisling Leprechaun");
        assertThat(gqs.getEffectiveColors(gd, blocker)).containsExactly(CardColor.GREEN);
    }
}
