package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HuntingTriad;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SphinxOfJwarIsle;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromanticSelection.class, GrizzlyBears.class, HillGiant.class, HuntingTriad.class,
        SolRing.class, SphinxOfJwarIsle.class, SteelHellkite.class})
class NecromanticSelectionTest extends BaseCardTest {

    @Test
    void returnsOneDestroyedCreatureAsBlackZombieUnderYourControlAndExilesSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(preexistingCreature));
        NecromanticSelection selection = new NecromanticSelection();
        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(bears.getCard().getId(), giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(giant.getCard()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(giant.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, returned)).contains(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .contains(CardSubtype.GIANT, CardSubtype.ZOMBIE);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    void doesNotReturnCreatureAlreadyInGraveyard() {
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(preexistingCreature));
        NecromanticSelection selection = new NecromanticSelection();
        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    @CardUsed({NecromanticSelection.class, SphinxOfJwarIsle.class})
    void canReturnShroudedCreatureAndPreservesItsOriginalColor() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfJwarIsle());
        NecromanticSelection selection = new NecromanticSelection();

        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId).containsExactly(sphinx.getCard().getId());
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Sphinx of Jwar Isle");
        harness.assertNotOnBattlefield(player2, "Sphinx of Jwar Isle");
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Sphinx of Jwar Isle"));
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SPHINX, CardSubtype.ZOMBIE);
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    @CardUsed({NecromanticSelection.class, SteelHellkite.class, SolRing.class})
    void returnsColorlessArtifactCreatureAsBlackAndLeavesNoncreatureArtifactsAlone() {
        harness.addToBattlefield(player1, new SteelHellkite());
        harness.addToBattlefield(player2, new SolRing());
        NecromanticSelection selection = new NecromanticSelection();

        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Steel Hellkite"));
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactly(CardColor.BLACK);
        assertThat(gqs.isArtifact(returned)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.DRAGON, CardSubtype.ZOMBIE);
        assertThat(returned.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Sol Ring");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    void regeneratedCreatureIsNotOfferedForReturn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        NecromanticSelection selection = new NecromanticSelection();

        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId).containsExactly(giant.getCard().getId());
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    @CardUsed({NecromanticSelection.class, HuntingTriad.class})
    void destroyedCreatureTokensCannotBeChosenForReturn() {
        harness.castFromHand(player1, new HuntingTriad(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        NecromanticSelection selection = new NecromanticSelection();

        harness.castFromHand(player1, selection, "{4}{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }
}
