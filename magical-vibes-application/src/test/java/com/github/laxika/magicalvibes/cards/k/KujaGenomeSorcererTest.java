package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.q.QueenBrahne;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TranceKujaFateDefied;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KujaGenomeSorcerer.class, TranceKujaFateDefied.class, FugitiveWizard.class,
        QueenBrahne.class, Shock.class, TurnToFrog.class})
class KujaGenomeSorcererTest extends BaseCardTest {

    @Test
    void createsTappedWizardTokenAtControllerEndStep() {
        Permanent kuja = addKuja();

        advanceToEndStep();
        harness.passBothPriorities();

        Permanent token = findToken();
        assertThat(token.isTapped()).isTrue();
        assertThat(kuja.isTransformed()).isFalse();
    }

    @Test
    void transformsAfterCreatingTokenWithFourWizards() {
        Permanent kuja = addKuja();
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(kuja.isTransformed()).isTrue();
        assertThat(kuja.getCard()).isInstanceOf(TranceKujaFateDefied.class);
    }

    @Test
    void transformedKujaDoublesWizardDamage() {
        KujaGenomeSorcerer card = new KujaGenomeSorcerer();
        Permanent kuja = new Permanent(card);
        kuja.setCard(card.getBackFaceCard());
        kuja.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(kuja);

        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        wizard.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void transformedKujaDoublesWizardTokenDamage() {
        KujaGenomeSorcerer card = new KujaGenomeSorcerer();
        Permanent kuja = new Permanent(card);
        kuja.setCard(card.getBackFaceCard());
        kuja.setTransformed(true);
        addCreatureReady(player1, new QueenBrahne());
        gd.playerBattlefields.get(player1.getId()).add(kuja);

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void ownTokenDealsDamageForNoncreatureSpellWhileTapped() {
        addKuja();
        advanceToEndStep();
        resolveAllTriggers();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findToken().isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void ownTokenDoesNotTriggerForCreatureSpell() {
        addKuja();
        advanceToEndStep();
        resolveAllTriggers();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    void ownTokenDoesNotTriggerForOpponentSpell() {
        addKuja();
        advanceToEndStep();
        resolveAllTriggers();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentWizardsDoNotCountTowardTransformation() {
        Permanent kuja = addKuja();
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        advanceToEndStep();
        resolveAllTriggers();

        assertThat(kuja.isTransformed()).isFalse();
        assertThat(findPermanents(player1, "Wizard")).hasSize(1);
    }

    @Test
    void doesNotCreateTokenOnOpponentEndStep() {
        addKuja();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void transformedKujaDoublesOwnTokenButNotShockAndStopsCreatingTokens() {
        Permanent kuja = addKuja();
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        advanceToEndStep();
        resolveAllTriggers();
        assertThat(kuja.isTransformed()).isTrue();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        advanceToEndStep();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wizard")).hasSize(1);
        assertThat(kuja.isTransformed()).isTrue();
    }

    @Test
    void transformedKujaDoesNotDoubleOpponentWizardDamage() {
        harness.addToBattlefield(player1, new TranceKujaFateDefied());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        wizard.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    void losingAllAbilitiesStopsDoublingWizardDamage() {
        Permanent kuja = harness.addToBattlefieldAndReturn(player1, new TranceKujaFateDefied());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, kuja.getId());
        resolveAllTriggers();

        wizard.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
    }

    @Test
    void transformedKujaDoublesItsOwnCombatDamage() {
        Permanent kuja = harness.addToBattlefieldAndReturn(player1, new TranceKujaFateDefied());
        kuja.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 12);
    }

    @Test
    void transformedKujaDoublesDamageToPermanents() {
        addCreatureReady(player1, new TranceKujaFateDefied());
        addCreatureReady(player2, new TranceKujaFateDefied());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addKuja() {
        return harness.addToBattlefieldAndReturn(player1, new KujaGenomeSorcerer());
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
