package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnidentifiedHovership.class, GrizzlyBears.class, ColossalDreadmaw.class,
        Shatter.class, Forest.class, Panharmonicon.class})
class UnidentifiedHovershipTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger exiles up to one target creature with toughness 5 or less")
    void enterTriggerExilesEligibleCreature() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tooTough = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tooTough.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();

        Permanent hovership = findPermanent(player1, "Unidentified Hovership");
        assertThat(gd.getCardsExiledByPermanent(hovership.getId()))
                .extracting(Card::getId)
                .containsExactly(eligible.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("When it leaves, the exiled card's owner manifests dread")
    void leavesTriggerUsesExiledCardsOwnerAndPutsTheOtherCardInTheirGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent hovership = findPermanent(player1, "Unidentified Hovership");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hovership.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(graveyardCard);
    }

    @Test
    void crewAnimatesVehicleAndTapsSummoningSickCreature() {
        Permanent hovership = harness.addToBattlefieldAndReturn(player1, new UnidentifiedHovership());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        assertThat(gqs.isCreature(gd, hovership)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hovership)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(hovership.isTapped()).isFalse();
    }

    @Test
    void cannotCrewWithoutAnUntappedCreature() {
        harness.addToBattlefield(player1, new UnidentifiedHovership());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void leavingWithoutAnExiledCardDoesNotManifestDread() {
        Permanent hovership = harness.addToBattlefieldAndReturn(player1, new UnidentifiedHovership());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hovership);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.values().stream().flatMap(List::stream))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void eachOwnerManifestsDreadWhenAdditionalEnterTriggerExilesTwoCards() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card ownManifest = new Forest();
        Card opposingManifest = new Forest();
        harness.setLibrary(player1, List.of(ownManifest, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(opposingManifest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        resolveAllTriggers();

        Permanent hovership = findPermanent(player1, "Unidentified Hovership");
        assertThat(gd.getCardsExiledByPermanent(hovership.getId())).hasSize(2);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hovership);
        harness.passBothPriorities();

        Set<UUID> manifestingPlayers = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            PendingInteraction.LibraryRevealChoice choice = gd.interaction
                    .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice).isNotNull();
            assertThat(manifestingPlayers.add(choice.playerId())).isTrue();
            boolean ownChoice = choice.playerId().equals(player1.getId());
            harness.handleMultipleCardsChosen(ownChoice ? player1 : player2,
                    List.of((ownChoice ? ownManifest : opposingManifest).getId()));
        }

        assertThat(manifestingPlayers).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(ownManifest.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(opposingManifest.getId()));
    }

    @Test
    void enterTriggerCanChooseNoTargetDespiteAnEligibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Unidentified Hovership").getId()))
                .isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature.getCard());
    }

    @Test
    void enterTriggerCanExileCreatureWithEffectiveToughnessExactlyFive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
    }

    @Test
    void manifestsOnlyAvailableCardWhenOwnerHasOneCardInLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card lastCard = new Forest();
        harness.setLibrary(player2, List.of(lastCard));
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanent(player1, "Unidentified Hovership"));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(lastCard.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(lastCard);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(lastCard.getId()));
    }

    private void addHovershipMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
