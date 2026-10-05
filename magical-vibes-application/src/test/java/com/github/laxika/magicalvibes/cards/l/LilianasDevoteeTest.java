package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FinishingBlow;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasDevotee.class, WalkingCorpse.class, FinishingBlow.class})
class LilianasDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Zombies you control by +1/+0")
    void boostsOwnZombies() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent zombie = findPermanent(player1, "Walking Corpse");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Zombie")
    void doesNotBoostOpponentsZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Permanent zombie = findPermanent(player2, "Walking Corpse");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new LilianasDevotee());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Pays {1}{B} at the end step to create a Zombie")
    void paysToCreateZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment creates no Zombie")
    void declinesToCreateZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Multiple Devotees each boost your Zombies")
    void boostsStack() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent zombie = findPermanent(player1, "Walking Corpse");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        for (Permanent devotee : findPermanents(player1, "Liliana's Devotee")) {
            assertThat(gqs.getEffectivePower(gd, devotee)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("An actual opposing creature death enables one boosted token")
    void opposingCreatureDeathEnablesToken() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, findPermanent(player2, "Walking Corpse").getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Walking Corpse");

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature death does not trigger Devotee during an opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana cannot create a Zombie")
    void cannotCreateTokenWithoutFullPayment() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A death after the end step begins does not retroactively trigger Devotee")
    void deathDuringEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player2, new WalkingCorpse());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, findPermanent(player2, "Walking Corpse").getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Walking Corpse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Removing Devotee does not stop its already triggered token ability")
    void triggerResolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, findPermanent(player1, "Liliana's Devotee").getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Liliana's Devotee");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @CardUsed({RiseFromTheGrave.class})
    @DisplayName("Devotee also boosts itself when Rise from the Grave makes it a Zombie")
    void boostsItselfWhenItBecomesZombie() {
        harness.setGraveyard(player1, List.of(new LilianasDevotee()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent devotee = findPermanent(player1, "Liliana's Devotee");
        assertThat(gqs.getEffectivePower(gd, devotee)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devotee)).isEqualTo(3);
    }
}
