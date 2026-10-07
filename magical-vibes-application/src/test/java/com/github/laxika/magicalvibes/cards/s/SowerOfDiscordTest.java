package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArchfiendOfDespair;
import com.github.laxika.magicalvibes.cards.e.Exsanguinate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SowerOfDiscord.class, Shock.class, ArchfiendOfDespair.class, Exsanguinate.class})
class SowerOfDiscordTest extends BaseCardTest {

    @Test
    void damageToAChosenPlayerMakesTheOtherChosenPlayerLoseThatMuchLife() {
        castSowerOfDiscord();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void damageToTheOtherChosenPlayerAlsoTriggersLifeLoss() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageToACreatureDoesNotTriggerLifeLoss() {
        Permanent sower = castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sower.getId());
        resolveAllTriggers();

        assertThat(sower.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeLossWithoutDamageDoesNotTriggerLifeLoss() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 2);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void multipleSowersTriggerSeparatelyWithoutRetriggeringFromLifeLoss() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId(), player1.getId()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 18);
    }

    @Test
    void combatDamageToAChosenPlayerTriggersLifeLoss() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        Permanent attacker = addCreatureReady(player2, new ArchfiendOfDespair());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    void lethalCombatDamageToSowerDoesNotSuppressItsSimultaneousPlayerDamageTrigger() {
        Permanent sower = castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        Permanent blockedAttacker = addCreatureReady(player2, new ArchfiendOfDespair());
        Permanent unblockedAttacker = addCreatureReady(player2, new ArchfiendOfDespair());
        blockedAttacker.setAttacking(true);
        unblockedAttacker.setAttacking(true);
        sower.setBlocking(true);
        sower.addBlockingTarget(0);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sower of Discord");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    private Permanent castSowerOfDiscord() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SowerOfDiscord(), "{4}{B}{B}");
        harness.passBothPriorities();
        return findPermanent(player1, "Sower of Discord");
    }
}
