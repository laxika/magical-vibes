package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElectrostaticBlast.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, LavaAxe.class, Shock.class})
class ElectrostaticBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage and gives a one-time top-three play boon")
    void dealsDamageAndTriggersBoonOnNextInstantOrSorcery() {
        Card first = new Forest();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Forest();
        Card fifth = new Shock();
        Card sixth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);


        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Requires a creature, planeswalker, battle, or player target")
    void rejectsNonTargetablePermanent() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ElectrostaticBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayWaitUntilAfterTriggerResolutionToPlayOneExiledCard() {
        Card instant = new Shock();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(instant, creature, land));
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boonSurvivesTurnChangeAndIgnoresOpponentAndCreatureSpells() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ElectrostaticBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.assertLife(player2, 13);
    }

    @Test
    void illegalDamageTargetPreventsGrantingBoon() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void emptyLibraryStillConsumesTheOneTimeBoon() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 14);
    }

    @Test
    void exilesAllAvailableCardsAndPermissionExpiresAtEndOfTurn() {
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ElectrostaticBlast(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
