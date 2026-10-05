package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakeYourOwnLuck;
import com.github.laxika.magicalvibes.cards.o.OjerPakpatiq;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlickSequence;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilahUndefeatedSlickshot.class, Counterspell.class, GrizzlyBears.class, Shock.class,
        Terminate.class, SlickSequence.class, MakeYourOwnLuck.class, OjerPakpatiq.class})
class LilahUndefeatedSlickshotTest extends BaseCardTest {

    private Permanent addLilah() {
        Permanent lilah = harness.addToBattlefieldAndReturn(player1, new LilahUndefeatedSlickshot());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return lilah;
    }

    @Test
    @DisplayName("Prowess boosts Lilah when a noncreature spell is cast")
    void prowessBoostsForNoncreatureSpell() {
        Permanent lilah = addLilah();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, lilah)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lilah)).isEqualTo(4);
    }

    @Test
    @DisplayName("A multicolored instant cast from hand becomes plotted as it resolves")
    void plotsMulticoloredInstantFromHand() {
        addLilah();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Terminate terminate = new Terminate();
        harness.setHand(player1, List.of(terminate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(terminate);
        assertThat(gd.plottedCardIds).contains(terminate.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(terminate.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(terminate.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(terminate);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A monocolored instant is not plotted")
    void doesNotPlotMonocoloredInstant() {
        addLilah();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.plottedCardIds).doesNotContain(shock.getId());
    }

    @Test
    @DisplayName("A countered spell is not plotted")
    void doesNotPlotCounteredSpell() {
        addLilah();
        Terminate terminate = new Terminate();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(terminate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, terminate.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(terminate);
        assertThat(gd.plottedCardIds).doesNotContain(terminate.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(terminate);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent lilah = addLilah();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lilah)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lilah)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void spellWithNoLegalTargetIsNotPlotted() {
        addLilah();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Terminate spell = new Terminate();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.plottedCardIds).doesNotContain(spell.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    void plotTriggerSurvivesLilahLeavingTheBattlefield() {
        Permanent lilah = addLilah();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Terminate spell = new Terminate();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lilah.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.plottedCardIds).contains(spell.getId());
    }

    @Test
    void plotsMulticoloredSorceryAfterItsEffectsFinish() {
        addLilah();
        MakeYourOwnLuck spell = new MakeYourOwnLuck();
        SlickSequence drawn = new SlickSequence();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.plottedCardIds).contains(spell.getId());
    }

    @Test
    void plottedInstantCannotBeCastOnTheSameTurn() {
        SlickSequence spell = castAndPlotSlickSequence();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void plottedInstantCannotBeCastDuringCombatOnALaterTurn() {
        SlickSequence spell = castAndPlotSlickSequence();
        gd.turnNumber++;
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void plottedSpellCanBeCastForFreeLaterAndIsNotPlottedAgain() {
        SlickSequence spell = castAndPlotSlickSequence();
        harness.setLibrary(player1, List.of(new LilahUndefeatedSlickshot()));
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, spell.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.plottedCardIds).doesNotContain(spell.getId());
    }

    @Test
    void opponentsMulticoloredSpellDoesNotTriggerEitherAbility() {
        Permanent lilah = addLilah();
        SlickSequence spell = new SlickSequence();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lilah)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.plottedCardIds).doesNotContain(spell.getId());
    }

    @Test
    void controllerChoosesBetweenReboundAndPlot() {
        addLilah();
        harness.addToBattlefield(player1, new OjerPakpatiq());
        SlickSequence spell = new SlickSequence();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private SlickSequence castAndPlotSlickSequence() {
        addLilah();
        SlickSequence spell = new SlickSequence();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.plottedCardIds).contains(spell.getId());
        return spell;
    }
}
