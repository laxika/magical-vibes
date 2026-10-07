package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrygonPredator.class, AzoriusSignet.class, SealOfDoom.class, MistralCharger.class})
class TrygonPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may destroy an artifact or enchantment controlled by the damaged player")
    void acceptingTriggerDestroysArtifactOrEnchantment() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        Permanent enemyEnchantment = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());
        Permanent enemyCreature = addCreatureReady(player2, new MistralCharger());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(enemyArtifact.getId(), enemyEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());

        harness.handlePermanentChosen(player1, enemyEnchantment.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Seal of Doom");
        harness.assertInGraveyard(player2, "Seal of Doom");
        harness.assertOnBattlefield(player2, "Azorius Signet");
    }

    @Test
    @DisplayName("Declining the combat-damage trigger destroys nothing")
    void decliningTriggerDestroysNothing() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Azorius Signet");
    }

    @Test
    @DisplayName("No ability remains on the stack when the damaged player has no legal target")
    void noMatchingPermanentMeansNoAbilityOnStack() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        addCreatureReady(player2, new MistralCharger());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Accepting the trigger destroys the chosen artifact")
    void acceptingTriggerDestroysArtifact() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        resolveCombat();
        harness.assertLife(player2, 18);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Azorius Signet");
        harness.assertInGraveyard(player2, "Azorius Signet");
    }

    @Test
    @DisplayName("A sacrificed target does not allow choosing another permanent")
    void sacrificedTargetMakesAbilityFailToResolve() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());
        harness.addToBattlefield(player2, new AzoriusSignet());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.activateAbility(player2, 0, null, predator.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Seal of Doom");
        harness.assertInGraveyard(player1, "Trygon Predator");
        harness.assertOnBattlefield(player2, "Azorius Signet");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if Trygon Predator leaves the battlefield")
    void destroyingSourceDoesNotStopTrigger() {
        Permanent predator = addCreatureReady(player1, new TrygonPredator());
        predator.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.addToBattlefield(player2, new SealOfDoom());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.activateAbility(player2, 1, null, predator.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Trygon Predator");
        harness.assertInGraveyard(player2, "Azorius Signet");
        harness.assertNotOnBattlefield(player2, "Azorius Signet");
    }
}
