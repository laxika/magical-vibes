package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConjurersMantle.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, Shock.class})
class ConjurersMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger offers a matching creature card and puts the choice into hand")
    void attackTriggerOffersMatchingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());
        Card matching = new GrizzlyBears();
        Card nonmatching = new LlanowarElves();
        Card instant = new Shock();
        Card land = new Plains();
        Card otherNonmatching = new LlanowarElves();
        Card otherInstant = new Shock();
        harness.setLibrary(player1, List.of(matching, nonmatching, instant, land,
                otherNonmatching, otherInstant));

        declareAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                nonmatching, instant, land, otherNonmatching, otherInstant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the top six cards are considered")
    void onlyLooksAtTopSixCards() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());
        Card matchingBelowLook = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Plains(),
                new LlanowarElves(), new Shock(), new Plains(), matchingBelowLook));

        declareAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(matchingBelowLook);
    }

    @Test
    @DisplayName("Does not trigger when unattached")
    void doesNotTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addMantle(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof ConjurersMantle);
    }

    private Permanent addMantle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ConjurersMantle());
    }

    @Test
    void equipPaysOneManaAndAttachesToOwnCreature() {
        Permanent mantle = addMantle(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void mayDeclineMatchingCardFromShortLibrary() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMantle(player1).setAttachedTo(creature.getId());
        Card matching = new GrizzlyBears();
        Card other = new Plains();
        harness.setLibrary(player1, List.of(matching, other));

        declareAttackers(List.of(0));
        resolveAttackTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(matching, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMantle(player1).setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAttackTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChooseOnlyOneMatchingCardAndPutsRestBelowUnlookedCards() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMantle(player1).setAttachedTo(creature.getId());
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card elf = new LlanowarElves();
        Card land = new Plains();
        Card instant = new Shock();
        Card otherLand = new Plains();
        Card unlooked = new Shock();
        harness.setLibrary(player1, List.of(firstBear, secondBear, elf, land, instant, otherLand, unlooked));

        declareAttackers(List.of(0));
        resolveAttackTrigger();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(elf.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstBear).doesNotContain(secondBear);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unlooked);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(secondBear, elf, land, instant, otherLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void anotherCreatureAttackingDoesNotTriggerMantle() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());
        addMantle(player1).setAttachedTo(equipped.getId());

        declareAttackers(List.of(1));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof ConjurersMantle);
    }

    @Test
    void removingMantleDoesNotStopItsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(creature.getId());
        Card matching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(matching));

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(mantle);
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
    }

    @Test
    void usesCreatureTypesFromBeforeEquippedCreatureDied() {
        Permanent creature = addCreatureReady(player1, new LlanowarElves());
        addMantle(player1).setAttachedTo(creature.getId());
        Card matching = new LlanowarElves();
        harness.setLibrary(player1, List.of(matching));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player2, 0, creature.getId());
        resolveAttackTrigger();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
    }

    @Test
    void changingAttachmentDoesNotChangeWhichCreatureTriggeredTheAbility() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new LlanowarElves());
        Permanent mantle = addMantle(player1);
        mantle.setAttachedTo(attacker.getId());
        Card bear = new GrizzlyBears();
        Card elf = new LlanowarElves();
        harness.setLibrary(player1, List.of(bear, elf));

        declareAttackers(List.of(0));
        mantle.setAttachedTo(other.getId());
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(bear).doesNotContain(elf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
    }

    private void resolveAttackTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
