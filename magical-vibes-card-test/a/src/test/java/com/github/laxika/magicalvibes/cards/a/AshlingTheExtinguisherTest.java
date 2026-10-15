package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshlingTheExtinguisher.class, NettleSentinel.class, CascadeBluffs.class, SlipperyBogle.class})
class AshlingTheExtinguisherTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player prompts to choose a creature that player controls")
    void promptsToChooseCreature() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new NettleSentinel());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds())
                .contains(enemyCreature.getId());
    }

    @Test
    @DisplayName("The chosen creature is sacrificed and the game advances")
    void sacrificesChosenCreature() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new NettleSentinel());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nettle Sentinel");
        harness.assertInGraveyard(player2, "Nettle Sentinel");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Only the damaged player's creatures are valid choices (not own creatures, not lands)")
    void onlyDamagedPlayersCreatures() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new NettleSentinel());
        Permanent enemyCreature = addCreatureReady(player2, new NettleSentinel());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new CascadeBluffs());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(enemyCreature.getId())
                .doesNotContain(ownCreature.getId())
                .doesNotContain(enemyLand.getId());
    }

    @Test
    @DisplayName("No trigger when the damaged player controls no creatures")
    void noTriggerWithoutCreatures() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        harness.addToBattlefield(player2, new CascadeBluffs());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ashling does not trigger when blocked and deals combat damage only to a creature")
    void noTriggerWhenBlocked() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NettleSentinel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opposing hexproof creature cannot be targeted by Ashling's trigger")
    void excludesOpposingHexproofCreature() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent legalTarget = addCreatureReady(player2, new NettleSentinel());
        Permanent hexproofCreature = addCreatureReady(player2, new SlipperyBogle());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nettle Sentinel");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hexproofCreature);
    }

    @Test
    @DisplayName("Ashling cannot sacrifice the damaged player's only creature when it has hexproof")
    void noLegalTargetWhenOnlyCreatureHasHexproof() {
        Permanent ashling = addCreatureReady(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent hexproofCreature = addCreatureReady(player2, new SlipperyBogle());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hexproofCreature);
        assertThat(gd.stack).isEmpty();
    }
}
