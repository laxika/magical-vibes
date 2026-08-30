package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.q.QueenBrahne;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TranceKujaFateDefied;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KujaGenomeSorcerer.class, TranceKujaFateDefied.class, FugitiveWizard.class,
        QueenBrahne.class, Shock.class})
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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
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
        harness.passBothPriorities();
    }
}
