package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.a.AkromaAngelOfFury;
import com.github.laxika.magicalvibes.cards.a.AllosaurusShepherd;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FaerieSnoop;
import com.github.laxika.magicalvibes.cards.f.FaerieTrickery;
import com.github.laxika.magicalvibes.cards.f.Flashfreeze;
import com.github.laxika.magicalvibes.cards.g.Gainsay;
import com.github.laxika.magicalvibes.cards.g.GrimoireThief;
import com.github.laxika.magicalvibes.cards.i.IllusionaryMask;
import com.github.laxika.magicalvibes.cards.m.MysticalDispute;
import com.github.laxika.magicalvibes.cards.n.NeutralizingBlast;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pyroblast;
import com.github.laxika.magicalvibes.cards.r.RejectImperfection;
import com.github.laxika.magicalvibes.cards.s.SilverquillSilencer;
import com.github.laxika.magicalvibes.cards.s.SpellSnare;
import com.github.laxika.magicalvibes.cards.s.SternScolding;
import com.github.laxika.magicalvibes.cards.t.TalesEnd;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Effects that care about the characteristics of a spell see only the face-down spell's, of which there are no color,
 * name, subtype or supertype and no mana cost (CR 708.2, 708.4).
 */
@CardUsed({AinokSurvivalist.class, AkromaAngelOfFury.class, AllosaurusShepherd.class, Annul.class, Cancel.class,
        FaerieSnoop.class, FaerieTrickery.class, Flashfreeze.class, Gainsay.class, GrimoireThief.class, IllusionaryMask.class,
        MysticalDispute.class, NeutralizingBlast.class, Ornithopter.class, Pyroblast.class, RejectImperfection.class,
        SilverquillSilencer.class, SpellSnare.class, SternScolding.class, TalesEnd.class, Willbender.class})
class FaceDownSpellTargetingTest extends BaseCardTest {

    @Test
    @DisplayName("A blue card cast face down can't be targeted as a blue spell")
    void faceDownSpellIsNotBlue() {
        assertNoTarget(new Gainsay(), castFaceDown(new Willbender()));
    }

    @Test
    @DisplayName("A green card cast face down can't be targeted as a red or green spell")
    void faceDownSpellIsNotGreen() {
        assertNoTarget(new Flashfreeze(), castFaceDown(new AinokSurvivalist()));
    }

    @Test
    @DisplayName("A two-colored card cast face down can't be targeted as a multicolored spell")
    void faceDownSpellIsNotMulticolored() {
        assertNoTarget(new NeutralizingBlast(), castFaceDown(new FaerieSnoop()));
    }

    @Test
    @DisplayName("A legendary card cast face down can't be targeted as a legendary spell")
    void faceDownSpellIsNotLegendary() {
        assertNoTarget(new TalesEnd(), castFaceDown(new AkromaAngelOfFury()));
    }

    @Test
    @DisplayName("A card with mana value 2 cast face down can't be targeted as a spell with mana value 2")
    void faceDownSpellHasNoManaValue() {
        assertNoTarget(new SpellSnare(), castFaceDown(new Willbender()));
    }

    @Test
    @DisplayName("An artifact creature cast face down can't be targeted as an artifact spell")
    void faceDownSpellIsNotAnArtifact() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        Card thopter = new Ornithopter();
        harness.setHand(player1, List.of(thopter));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertNoTarget(new Annul(), thopter);
    }

    @Test
    @DisplayName("A Faerie cast face down can be countered as a non-Faerie spell")
    void faceDownSpellHasNoSubtype() {
        Card snoop = castFaceDown(new FaerieSnoop());

        assertThat(offeredTargets(new FaerieTrickery())).containsExactly(harness.stackIdOf(snoop));
        respond(new FaerieTrickery(), snoop);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(snoop.getId())).isNotNull();
    }

    @Test
    @DisplayName("A 6/6 cast face down can be countered as a spell with power or toughness 2 or less")
    void faceDownSpellIsTwoTwo() {
        Card akroma = castFaceDown(new AkromaAngelOfFury());

        assertThat(offeredTargets(new SternScolding())).containsExactly(harness.stackIdOf(akroma));
        respond(new SternScolding(), akroma);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(akroma);
    }

    @Test
    @DisplayName("The same card cast face up is still a blue spell")
    void faceUpSpellIsStillBlue() {
        Card willbender = new Willbender();
        harness.setHand(player1, List.of(willbender));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);

        assertThat(offeredTargets(new Gainsay())).containsExactly(willbender.getId());
        respond(new Gainsay(), willbender);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(willbender);
    }

    @Test
    @DisplayName("A blue card cast face down isn't countered by an effect that counters a spell if it's blue")
    void resolutionDoesNotSeeColor() {
        Card willbender = castFaceDown(new Willbender());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Pyroblast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, 0, harness.stackIdOf(willbender));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(willbender);
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(willbender.getId());
    }

    @Test
    @DisplayName("A countered face-down spell's mana value was 0")
    void lastKnownManaValueIsZero() {
        Permanent silencer = harness.addToBattlefieldAndReturn(player2, new SilverquillSilencer());
        silencer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card akroma = castFaceDown(new AkromaAngelOfFury());
        respond(new RejectImperfection(), akroma);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(akroma);
        assertThat(gd.interaction.isAwaitingInput()).as("proliferate").isTrue();
    }

    @Test
    @DisplayName("A spell that costs less if it targets a blue spell costs full price for a face-down one")
    void costDoesNotSeeColor() {
        Card willbender = castFaceDown(new Willbender());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new MysticalDispute()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harness.stackIdOf(willbender)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A trigger for spells with a chosen name doesn't see a face-down spell's name")
    void triggerDoesNotSeeName() {
        Permanent silencer = harness.addToBattlefieldAndReturn(player2, new SilverquillSilencer());
        silencer.setChosenName("Willbender");

        Card willbender = castFaceDown(new Willbender());

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(willbender.getId());
    }

    @Test
    @DisplayName("A green card cast face down isn't protected as a green spell from being countered")
    void counterProtectionDoesNotSeeColor() {
        harness.addToBattlefield(player1, new AllosaurusShepherd());
        Card survivalist = castFaceDown(new AinokSurvivalist());
        respond(new Cancel(), survivalist);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(survivalist);
    }

    @Test
    @DisplayName("An effect that counters spells with given names doesn't counter a face-down one")
    void namedSpellsDoNotIncludeFaceDownSpell() {
        Permanent thief = addCreatureReady(player2, new GrimoireThief());
        gd.addToExile(player1.getId(), new Willbender(), thief.getId());
        Card willbender = castFaceDown(new Willbender());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(willbender);
    }

    private Card castFaceDown(Card card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        return card;
    }

    private List<UUID> offeredTargets(Card response) {
        return GameTestEngineContext.get().getBean(ValidTargetService.class)
                .computeValidTargetsForSpell(gd, response, player2.getId(), List.of()).validPermanentIds();
    }

    private void respond(Card response, Card target) {
        UUID targetId = harness.stackIdOf(target);
        harness.passPriority(player1);
        harness.setHand(player2, List.of(response));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castInstant(player2, 0, targetId);
    }

    private void assertNoTarget(Card response, Card target) {
        assertThat(offeredTargets(response)).doesNotContain(harness.stackIdOf(target));
        assertThatThrownBy(() -> respond(response, target)).isInstanceOf(IllegalStateException.class);
    }
}
