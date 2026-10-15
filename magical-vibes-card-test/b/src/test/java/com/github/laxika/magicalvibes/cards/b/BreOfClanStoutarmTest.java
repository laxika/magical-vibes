package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheClachan;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProtectiveResponse;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreOfClanStoutarm.class, Forest.class, GrizzlyBears.class, ShivanDragon.class,
        ProtectiveResponse.class, ChampionOfTheClachan.class})
class BreOfClanStoutarmTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying and lifelink to another creature until end of turn")
    void grantsKeywordsUntilEndOfTurn() {
        addCreatureReady(player1, new BreOfClanStoutarm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target Bre herself")
    void cannotTargetSelf() {
        addCreatureReady(player1, new BreOfClanStoutarm());
        UUID breId = harness.getPermanentId(player1, "Bre of Clan Stoutarm");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, breId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("End-step trigger exiles lands and offers an eligible nonland card")
    void offersEligibleNonlandCard() {
        addBreWithLifeGain(List.of(new Forest(), new GrizzlyBears()), 2);

        resolveBreEndStepTrigger();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Accepting the offer casts the eligible card for free")
    void acceptsAndCastsEligibleCard() {
        addBreWithLifeGain(List.of(new Forest(), new GrizzlyBears()), 2);

        resolveBreEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A card above the life-gain amount goes directly to hand")
    void highManaValueCardGoesToHand() {
        addBreWithLifeGain(List.of(new Forest(), new ShivanDragon()), 2);

        resolveBreEndStepTrigger();

        harness.assertInHand(player1, "Shivan Dragon");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The end-step trigger does not happen without life gain")
    void noTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new BreOfClanStoutarm());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new BreOfClanStoutarm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new BreOfClanStoutarm());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allLandLibraryIsExiledWithoutAnOffer() {
        addBreWithLifeGain(List.of(new Forest(), new Forest()), 2);

        resolveBreEndStepTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPromptOrDraw() {
        addBreWithLifeGain(List.of(), 2);

        resolveBreEndStepTrigger();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        addBreWithLifeGain(List.of(new Forest(), new GrizzlyBears()), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void lifeGainedInResponseIncreasesTheCastingLimit() {
        addBreWithLifeGain(List.of(new ShivanDragon()), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.lifeGainedThisTurn.put(player1.getId(), 6);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInHand(player1, "Shivan Dragon");
    }

    @Test
    void eligibleSpellWithoutLegalTargetsGoesToHand() {
        addBreWithLifeGain(List.of(new ProtectiveResponse()), 3);

        resolveBreEndStepTrigger();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Protective Response");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Protective Response"));
    }

    @Test
    void cannotCastWithoutPayingMandatoryBeholdCost() {
        addBreWithLifeGain(List.of(new ChampionOfTheClachan()), 4);

        resolveBreEndStepTrigger();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Champion of the Clachan"));
        harness.assertInHand(player1, "Champion of the Clachan");
        assertThat(gd.exiledCards).isEmpty();
    }

    private void addBreWithLifeGain(List<Card> library, int lifeGained) {
        harness.addToBattlefield(player1, new BreOfClanStoutarm());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        gd.lifeGainedThisTurn.put(player1.getId(), lifeGained);
    }

    private void resolveBreEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
