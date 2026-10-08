package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienReid.class, Forest.class, GrizzlyBears.class, IntangibleVirtue.class,
        LiquimetalCoating.class, SerraAngel.class, Shock.class})
class VivienReidTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a chosen creature or land into hand and randomizes the rest on the bottom")
    void plusOneChoosesCreatureOrLand() {
        Permanent vivien = addReadyVivien(player1);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card flyer = new SerraAngel();
        harness.setLibrary(player1, List.of(creature, land, spell, flyer));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards())
                .containsExactly(creature, land, spell, flyer);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell, flyer);
    }

    @Test
    @DisplayName("-3 destroys an artifact")
    void minusThreeDestroysArtifact() {
        Permanent vivien = addReadyVivien(player1);
        harness.addToBattlefield(player2, new LiquimetalCoating());
        UUID artifactId = harness.getPermanentId(player2, "Liquimetal Coating");

        harness.activateAbility(player1, 0, 1, null, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liquimetal Coating");
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 destroys an enchantment and a creature with flying, but not a ground creature")
    void minusThreeTargetRestrictions() {
        Permanent vivien = addReadyVivien(player1);
        harness.addToBattlefield(player2, new IntangibleVirtue());
        Permanent enchantment = findPermanent(player2, "Intangible Virtue");

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Intangible Virtue");

        vivien.setCounterCount(CounterType.LOYALTY, 5);
        vivien.setLoyaltyActivationsThisTurn(0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player2, new SerraAngel());
        Permanent angel = findPermanent(player2, "Serra Angel");
        harness.activateAbility(player1, 0, 1, null, angel.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("-8 gives the controller's creatures +2/+2 and three keywords")
    void ultimateCreatesCreatureBoostEmblem() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 8);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opponentCreature = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void plusOneMayDeclineEvenWithEligibleCards() {
        addReadyVivien(player1);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card flyer = new SerraAngel();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(creature, land, spell, flyer, untouched));
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(creature, land, spell, flyer);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void plusOneRejectsNoncreatureNonlandAndMultipleSelections() {
        addReadyVivien(player1);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(creature, land, spell, new Shock(), untouched));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(spell.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature).doesNotContain(land, spell);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).contains(land, spell);
    }

    @Test
    void plusOneWithNoEligibleCardsPutsAllLookedAtCardsOnBottom() {
        addReadyVivien(player1);
        List<Card> lookedAt = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1),
                lookedAt.get(2), lookedAt.get(3), untouched));
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void plusOneWorksWithFewerThanFourCards() {
        addReadyVivien(player1);
        Card land = new Forest();
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(land, spell));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void plusOneWithEmptyLibraryStillAddsLoyalty() {
        Permanent vivien = addReadyVivien(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateAppliesToFutureCreaturesAfterVivienLeavesBattlefield() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vivien Reid");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void minusThreeCanDestroyOwnArtifactAfterVivienDiesToLoyaltyCost() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vivien Reid");
        harness.assertNotOnBattlefield(player1, "Liquimetal Coating");
        harness.assertInGraveyard(player1, "Vivien Reid");
        harness.assertInGraveyard(player1, "Liquimetal Coating");
    }

    @Test
    void minusThreeCannotDestroyFlyingCreatureProtectedByEmblem() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        addReadyVivien(player2);

        harness.activateAbility(player2, 0, 1, null, angel.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertNotInGraveyard(player1, "Serra Angel");
    }

    private Permanent addReadyVivien(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VivienReid());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
