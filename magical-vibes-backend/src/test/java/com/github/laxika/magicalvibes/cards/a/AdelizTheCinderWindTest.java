package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdelizTheCinderWindTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts all Wizards you control +1/+1")
    void castingInstantBoostsWizards() {
        Permanent adeliz = addReadyAdeliz(player1);
        Permanent wizard = addReadyPermanent(player1, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        // Resolve spell cast trigger
        harness.passBothPriorities();

        // Both Adeliz (Human Wizard) and Fugitive Wizard should get +1/+1
        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);
        assertThat(wizard.getPowerModifier()).isEqualTo(1);
        assertThat(wizard.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an instant does not boost non-Wizard creatures")
    void castingInstantDoesNotBoostNonWizards() {
        addReadyAdeliz(player1);
        Permanent bears = addReadyPermanent(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears (Bear) should not get the boost
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a creature does not trigger the boost")
    void castingCreatureDoesNotTrigger() {
        Permanent adeliz = addReadyAdeliz(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(0);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple instant casts stack the boost")
    void multipleInstantCastsStackBoost() {
        Permanent adeliz = addReadyAdeliz(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(1);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(1);

        // Resolve Shock
        harness.passBothPriorities();

        // Cast second instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(adeliz.getPowerModifier()).isEqualTo(2);
        assertThat(adeliz.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's Wizards")
    void doesNotBoostOpponentWizards() {
        addReadyAdeliz(player1);
        Permanent opponentWizard = addReadyPermanent(player2, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Opponent's Wizard should not get the boost
        assertThat(opponentWizard.getPowerModifier()).isEqualTo(0);
        assertThat(opponentWizard.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addReadyAdeliz(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyPermanent(player, new AdelizTheCinderWind());
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                         com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
