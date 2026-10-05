package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortalToPhyrexia.class, ArgothianSprite.class})
class PortalToPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent chooses three creatures to sacrifice when it enters")
    void eachOpponentSacrificesThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent spared = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());

        harness.castFromHand(player1, new PortalToPhyrexia(), "{9}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(spared.getId());
    }

    @Test
    @DisplayName("Upkeep returns a target creature from any graveyard as a Phyrexian")
    void upkeepReturnsCreatureFromAnyGraveyardAsPhyrexian() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        Card creature = new ArgothianSprite();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Argothian Sprite");
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.PHYREXIAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.FAERIE)).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Upkeep does not trigger without a creature card in any graveyard")
    void upkeepDoesNotTriggerWithoutCreatureCard() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        harness.setGraveyard(player2, List.of(new PortalToPhyrexia()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent sacrifices all their creatures when they control fewer than three")
    void sacrificesAvailableCreaturesAndLeavesOtherPermanentsAndControllerAlone() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new PortalToPhyrexia());

        harness.castFromHand(player1, new PortalToPhyrexia(), "{9}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(otherArtifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).contains(first.getCard().getId(), second.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(ownCreature.getId());
    }

    @Test
    @DisplayName("Upkeep can return a creature from the controller's graveyard")
    void upkeepReturnsOwnCreature() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        Card creature = new ArgothianSprite();
        Card opposingCreature = new ArgothianSprite();
        Card noncreature = new PortalToPhyrexia();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        advanceToUpkeep(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.cards()).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), opposingCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Argothian Sprite");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.PHYREXIAN)).isTrue();
        harness.assertNotInGraveyard(player1, "Argothian Sprite");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(noncreature.getId());
        harness.assertInGraveyard(player2, "Argothian Sprite");
    }

    @Test
    @DisplayName("Portal does not reanimate during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        Card creature = new ArgothianSprite();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Argothian Sprite");
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
    }

    @Test
    @DisplayName("A creature that leaves the graveyard in response is not replaced by another target")
    void removedTargetDoesNotReanimateAnotherCreature() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        Card target = new ArgothianSprite();
        Card other = new ArgothianSprite();
        harness.setGraveyard(player2, List.of(target, other));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(other.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
