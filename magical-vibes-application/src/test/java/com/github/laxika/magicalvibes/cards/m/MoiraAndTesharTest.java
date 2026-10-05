package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoiraAndTeshar.class, GrizzlyBears.class, Spellbook.class, Forest.class, Terminate.class, Pacifism.class})
class MoiraAndTesharTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic spell targets a nonland permanent card in the graveyard")
    void historicSpellTargetsNonlandPermanent() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land card in the graveyard is not a valid target")
    void landCardIsNotValidTarget() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The returned permanent is exiled at the beginning of the next end step")
    void returnedPermanentIsExiledAtNextEndStep() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The returned permanent is exiled instead of going to the graveyard when destroyed")
    void returnedPermanentIsExiledIfItWouldLeaveBattlefield() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    void nonhistoricCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        Spellbook target = new Spellbook();
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    void historicSpellCanReturnNoncreaturePermanent() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        Spellbook target = new Spellbook();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    void eachHistoricSpellTriggersInTheSameTurn() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnedAuraEntersAttachedToChosenCreature() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Pacifism aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent returned = findPermanent(player1, "Pacifism");
        assertThat(returned.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void opponentsHistoricSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Spellbook(), "{0}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotReturnCardFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void legendaryCreatureSpellTriggersBeforeItResolves() {
        harness.addToBattlefield(player1, new MoiraAndTeshar());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        harness.castFromHand(player1, new MoiraAndTeshar(), "{3}{W}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }
}
