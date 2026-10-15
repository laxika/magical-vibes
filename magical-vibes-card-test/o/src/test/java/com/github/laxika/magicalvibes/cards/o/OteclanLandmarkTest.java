package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IronpawAspirant;
import com.github.laxika.magicalvibes.cards.m.MinersGuidewing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OteclanLandmark.class, OteclanLevitator.class, IronpawAspirant.class,
        MinersGuidewing.class})
class OteclanLandmarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by scrying two")
    void entersWithScryTwo() {
        harness.setHand(player1, List.of(new OteclanLandmark()));
        Card topCard = new IronpawAspirant();
        harness.setLibrary(player1, List.of(topCard, new OteclanLandmark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(topCard);
    }

    @Test
    @DisplayName("Craft returns Oteclan Landmark transformed")
    void craftReturnsTransformed() {
        harness.addToBattlefield(player1, new OteclanLandmark());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new OteclanLandmark());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent levitator = findPermanent(player1, "Oteclan Levitator");
        assertThat(levitator.isTransformed()).isTrue();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Oteclan Levitator gives flying to an attacking creature without flying")
    void attackTriggerGivesFlyingToAttackingCreatureWithoutFlying() {
        OteclanLandmark front = new OteclanLandmark();
        Permanent levitator = new Permanent(front);
        levitator.setCard(front.getBackFaceCard());
        levitator.setTransformed(true);
        levitator.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(levitator);
        Permanent aspirant = addCreatureReady(player1, new IronpawAspirant());
        Permanent guidewing = addCreatureReady(player1, new MinersGuidewing());
        Permanent nonattacker = addCreatureReady(player1, new IronpawAspirant());

        declareAttackers(player1, List.of(0, 1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(aspirant.getId());
        harness.handlePermanentChosen(player1, aspirant.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, guidewing, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.FLYING)).isFalse();

        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();
    }

    @Test
    void craftCanExileArtifactFromGraveyard() {
        Card source = new OteclanLandmark();
        Card material = new OteclanLandmark();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(source.getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Oteclan Levitator").isTransformed()).isTrue();
        assertThat(gd.findExiledCard(source.getId())).isNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
    }

    @Test
    void craftCannotUseItselfNonartifactOrOpponentsArtifact() {
        harness.addToBattlefield(player1, new OteclanLandmark());
        harness.addToBattlefield(player1, new IronpawAspirant());
        harness.addToBattlefield(player2, new OteclanLandmark());
        harness.setGraveyard(player1, List.of(new IronpawAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Oteclan Landmark");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void craftCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new OteclanLandmark());
        harness.addToBattlefield(player1, new OteclanLandmark());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void scryCanPutBothCardsOnBottomInChosenOrder() {
        Card first = new IronpawAspirant();
        Card second = new MinersGuidewing();
        Card third = new OteclanLandmark();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new OteclanLandmark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    void attackWithOnlyFlyingCreaturesDoesNotGrantFlyingToNonattacker() {
        addCreatureReady(player1, new OteclanLevitator());
        addCreatureReady(player1, new MinersGuidewing());
        Permanent nonattacker = addCreatureReady(player1, new IronpawAspirant());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.FLYING)).isFalse();
    }
}
