package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AssassinsStrike;
import com.github.laxika.magicalvibes.cards.c.CodexShredder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DryadMilitant.class, AssassinsStrike.class, CodexShredder.class, GrizzlyBears.class, MindRot.class, Shock.class, TurnToFrog.class})
class DryadMilitantTest extends BaseCardTest {

    @Test
    @DisplayName("An instant resolved while Dryad Militant is on the battlefield is exiled")
    void resolvedInstantIsExiled() {
        harness.addToBattlefield(player1, new DryadMilitant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("A non-instant or non-sorcery card still goes to its graveyard")
    void nonSpellCardStillEntersGraveyard() {
        harness.addToBattlefield(player1, new DryadMilitant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An instant dealing lethal damage to Dryad Militant is exiled after it resolves")
    void lethalInstantLeavesWithDryad() {
        harness.addToBattlefield(player1, new DryadMilitant());
        UUID dryadId = harness.getPermanentId(player1, "Dryad Militant");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();

        harness.castAndResolveInstant(player2, 0, dryadId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dryad Militant"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Losing all abilities disables the graveyard replacement immediately")
    void abilityLossDisablesReplacement() {
        UUID dryadId = harness.addToBattlefieldAndReturn(player1, new DryadMilitant()).getId();
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dryadId);

        harness.assertInGraveyard(player1, "Turn to Frog");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Dryad Militant");
    }

    @Test
    @DisplayName("Sorceries and discarded instants and sorceries are exiled for both players")
    void sorceryAndDiscardedSpellsAreExiled() {
        harness.addToBattlefield(player1, new DryadMilitant());
        harness.setHand(player1, List.of(new MindRot()));
        harness.setHand(player2, List.of(new Shock(), new MindRot(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player1, "Mind Rot");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Mind Rot");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactlyInAnyOrder("Shock", "Mind Rot");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell that directly destroys the only Dryad Militant enters the graveyard")
    void directDestructionDisablesReplacementBeforeSpellLeavesStack() {
        UUID dryadId = harness.addToBattlefieldAndReturn(player2, new DryadMilitant()).getId();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, dryadId);

        harness.assertInGraveyard(player2, "Dryad Militant");
        harness.assertInGraveyard(player1, "Assassin's Strike");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An instant milled from a library is exiled without entering the graveyard")
    void milledInstantIsExiled() {
        harness.addToBattlefield(player1, new CodexShredder());
        harness.addToBattlefield(player2, new DryadMilitant());
        Shock milledCard = new Shock();
        harness.setLibrary(player1, List.of(milledCard, new MindRot()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(milledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(milledCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(milledCard);
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
