package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FetchQuest;
import com.github.laxika.magicalvibes.cards.c.CoopedUp;
import com.github.laxika.magicalvibes.cards.f.FranticFirebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrambleFamiliar.class, FetchQuest.class, Forest.class, GrizzlyBears.class, Pacifism.class,
        Shock.class, CoopedUp.class, FranticFirebolt.class, RestInPeace.class})
class BrambleFamiliarTest extends BaseCardTest {

    @Test
    void tapsForGreen() {
        Permanent familiar = addCreatureReady(player1, new BrambleFamiliar());
        forceMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    void discardingAControllerCardReturnsItToItsOwnersHand() {
        BrambleFamiliar card = new BrambleFamiliar();
        Permanent familiar = addCreatureReady(player1, card);
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void adventureMillsSevenAndPutsAChosenCreatureEnchantmentOrLandOntoTheBattlefield() {
        BrambleFamiliar card = new BrambleFamiliar();
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        Card land = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, new Shock(), enchantment, land,
                new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment, land);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCreatesNoChoiceWhenNoEligibleCardWasMilled() {
        BrambleFamiliar card = new BrambleFamiliar();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void bouncePaysDiscardAndTapCostsBeforeResolving() {
        BrambleFamiliar card = new BrambleFamiliar();
        Permanent familiar = addCreatureReady(player1, card);
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);
        forceMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(familiar.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(familiar);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
    }

    @Test
    void stolenFamiliarReturnsToItsOwnerWhileItsControllerPaysTheDiscardCost() {
        BrambleFamiliar card = new BrambleFamiliar();
        Permanent familiar = addCreatureReady(player1, card);
        gd.stolenCreatures.put(familiar.getId(), player2.getId());
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);
        forceMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
    }

    @Test
    void bounceCannotBeActivatedWithoutACardToDiscard() {
        Permanent familiar = addCreatureReady(player1, new BrambleFamiliar());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(familiar.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSicknessPreventsBothTapAbilities() {
        harness.addToBattlefield(player1, new BrambleFamiliar());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureCannotChooseAnOlderGraveyardCardOrDeclineAnEligibleMilledCard() {
        Card oldCreature = new BrambleFamiliar();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setHand(player1, List.of(new BrambleFamiliar()));
        harness.setLibrary(player1, List.of(land, new FranticFirebolt()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(land));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == land).singleElement()
                .satisfies(p -> assertThat(p.isTapped()).isFalse());
    }

    @Test
    void adventureMillsExactlySevenAndLeavesTheEighthCardInTheLibrary() {
        Card land = new Forest();
        Card eighth = new Forest();
        harness.setHand(player1, List.of(new BrambleFamiliar()));
        harness.setLibrary(player1, List.of(land, new FranticFirebolt(), new FranticFirebolt(),
                new FranticFirebolt(), new FranticFirebolt(), new FranticFirebolt(),
                new FranticFirebolt(), eighth));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eighth);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void adventurePutsAnAuraOntoTheBattlefieldAttachedToTheChosenCreature() {
        Permanent first = addCreatureReady(player1, new BrambleFamiliar());
        Permanent chosen = addCreatureReady(player2, new BrambleFamiliar());
        Card aura = new CoopedUp();
        harness.setHand(player1, List.of(new BrambleFamiliar()));
        harness.setLibrary(player1, List.of(aura, new FranticFirebolt()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == aura).singleElement()
                .satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(chosen.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
    }

    @Test
    void adventureOffersMilledCardsEvenWhenRestInPeaceReplacesTheirMoveToTheGraveyard() {
        harness.addToBattlefield(player2, new RestInPeace());
        Card land = new Forest();
        Card otherLand = new Forest();
        harness.setHand(player1, List.of(new BrambleFamiliar()));
        harness.setLibrary(player1, List.of(land, otherLand, new FranticFirebolt()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.findExiledCard(otherLand.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void adventureWithAnEmptyLibraryStillAllowsCastingTheCreatureFromExile() {
        BrambleFamiliar card = new BrambleFamiliar();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bramble Familiar");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
