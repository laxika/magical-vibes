package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PasswallAdept;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NivMizzetParun.class, Cancel.class, FugitiveWizard.class, GrizzlyBears.class,
        LightningBolt.class, DirectCurrent.class, PasswallAdept.class})
class NivMizzetParunTest extends BaseCardTest {

    @Test
    @DisplayName("An instant cast by any player draws a card and the draw deals 1 damage")
    void instantCastDrawsAndDealsDamage() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A creature spell does not trigger the card draw ability")
    void creatureCastDoesNotDraw() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Niv-Mizzet, Parun cannot be countered")
    void cannotBeCountered() {
        NivMizzetParun niv = new NivMizzetParun();
        harness.setHand(player1, List.of(niv));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, niv.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Niv-Mizzet, Parun");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("A controller-cast sorcery draws before resolving and triggers damage")
    void controllerSorceryDrawsBeforeResolving() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of(new DirectCurrent()));
        harness.setLibrary(player1, List.of(new PasswallAdept()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Passwall Adept");
        harness.assertLife(player2, 20);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        resolveAllTriggers();
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Direct Current");
    }

    @Test
    @DisplayName("An independent draw can damage a creature")
    void independentDrawDamagesCreature() {
        addReadyNiv(player1);
        harness.setLibrary(player1, List.of(new PasswallAdept()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PasswallAdept());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Passwall Adept");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Passwall Adept");
    }

    @Test
    @DisplayName("An opponent's draw does not trigger damage")
    void opponentDrawDoesNotTriggerDamage() {
        addReadyNiv(player1);
        harness.setLibrary(player2, List.of(new PasswallAdept()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Passwall Adept");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Drawing two cards creates two separate damage triggers")
    void multipleDrawsTriggerForEachCard() {
        addReadyNiv(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PasswallAdept(), new PasswallAdept()));
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyNiv(Player player) {
        return addCreatureReady(player, new NivMizzetParun());
    }
}
