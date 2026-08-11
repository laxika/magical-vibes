package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BreOfClanStoutarmTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability grants flying and lifelink to another creature until end of turn")
    void grantsKeywordsUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BreOfClanStoutarm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Bre of Clan Stoutarm").setSummoningSick(false);
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
        harness.addToBattlefield(player1, new BreOfClanStoutarm());
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Grizzly Bears");
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
