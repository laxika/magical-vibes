package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlindSeer;
import com.github.laxika.magicalvibes.cards.g.GhituFire;
import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectiveSphere.class, GhituFire.class, LlanowarElite.class, RagingKavu.class, BlindSeer.class})
class ProtectiveSphereTest extends BaseCardTest {

    @Test
    void coloredManaRestrictsSourceChoice() {
        addSphere();
        Permanent redSource = addCreatureReady(player2, new RagingKavu());
        Permanent greenSource = addCreatureReady(player2, new LlanowarElite());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(redSource.getId()).doesNotContain(greenSource.getId());

        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId())).contains(redSource.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(redSource.getId());
    }

    @Test
    void colorlessManaDoesNotAllowAChoice() {
        addSphere();
        addCreatureReady(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerSourceDamagePreventionIds).doesNotContainKey(player1.getId());
    }

    @Test
    void multicoloredSourceMatchesAnyActivationColor() {
        addSphere();
        Permanent redGreenSource = addCreatureReady(player2, new RagingKavu());
        Permanent greenSource = addCreatureReady(player2, new LlanowarElite());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(redGreenSource.getId()).doesNotContain(greenSource.getId());
    }

    @Test
    void preventsChosenSourceCombatDamageOnly() {
        harness.setLife(player1, 20);
        addSphere();
        Permanent redSource = addCreatureReady(player2, new RagingKavu());
        Permanent greenSource = addCreatureReady(player2, new LlanowarElite());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        redSource.setAttacking(true);
        greenSource.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        addSphere();
        Permanent redSource = addCreatureReady(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId())).contains(redSource.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceDamagePreventionIds).doesNotContainKey(player1.getId());
    }

    @Test
    void canChooseMatchingSpellOnStack() {
        harness.setLife(player1, 20);
        addSphere();
        GhituFire ghituFire = new GhituFire();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(ghituFire));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ghituFire.getId());

        harness.handlePermanentChosen(player1, ghituFire.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void doesNotPreventDamageAfterSourceLosesMatchingColor() {
        harness.setLife(player1, 20);
        addSphere();
        harness.addToBattlefield(player1, new BlindSeer());
        Permanent source = addCreatureReady(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, source.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 16);
    }

    @Test
    void choosingCreatureSpellAlsoPreventsDamageFromPermanentItBecomes() {
        harness.setLife(player1, 20);
        addSphere();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        RagingKavu source = new RagingKavu();
        harness.castFromHand(player2, source, "{1}{R}{G}");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    void doesNotPreventChosenSpellDamageToYourCreature() {
        harness.setLife(player1, 20);
        addSphere();
        Permanent creature = addCreatureReady(player1, new LlanowarElite());
        GhituFire source = new GhituFire();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(source));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, 1, creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Llanowar Elite");
    }

    private void addSphere() {
        harness.addToBattlefield(player1, new ProtectiveSphere());
    }
}
