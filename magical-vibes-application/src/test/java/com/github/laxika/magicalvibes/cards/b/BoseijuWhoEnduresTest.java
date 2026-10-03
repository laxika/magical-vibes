package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.j.JukaiNaturalist;
import com.github.laxika.magicalvibes.cards.k.KodamaOfTheWestTree;
import com.github.laxika.magicalvibes.cards.m.MirrorBox;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoseijuWhoEndures.class, BreedingPool.class, Forest.class,
        DarksteelCitadel.class, JukaiNaturalist.class, KodamaOfTheWestTree.class, MirrorBox.class})
class BoseijuWhoEnduresTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana")
    void addsGreenMana() {
        harness.addToBattlefield(player1, new BoseijuWhoEndures());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel destroys an opponent's nonbasic land and searches for a land with a basic land type")
    void channelDestroysAndSearches() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BreedingPool());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new BreedingPool(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boseiju, Who Endures");
        harness.assertInGraveyard(player2, "Breeding Pool");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Breeding Pool", "Forest");

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
    }

    @Test
    @DisplayName("Channel cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or nonbasic land");
    }

    @Test
    void channelPaysFullCostAndDiscardsBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirrorBox());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Boseiju, Who Endures");
        harness.assertOnBattlefield(player2, "Mirror Box");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mirror Box");
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest)
                .allMatch(p -> !p.isTapped());
    }

    @Test
    void legendaryCreatureReducesChannelCostAndEnchantmentIsLegalTarget() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JukaiNaturalist());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jukai Naturalist");
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void legendaryLandAndOpponentsLegendaryCreatureDoNotReduceCost() {
        harness.addToBattlefield(player1, new BoseijuWhoEndures());
        harness.addToBattlefield(player2, new KodamaOfTheWestTree());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirrorBox());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boseiju, Who Endures");
        harness.assertOnBattlefield(player2, "Mirror Box");
    }

    @Test
    void multipleLegendaryCreaturesCannotRemoveGreenCost() {
        harness.addToBattlefield(player1, new MirrorBox());
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirrorBox());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boseiju, Who Endures");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Boseiju, Who Endures");
    }

    @Test
    void cannotTargetOwnArtifactOrOpponentsOrdinaryCreature() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MirrorBox());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new KodamaOfTheWestTree());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Boseiju, Who Endures");
    }

    @Test
    void indestructibleTargetStillAllowsLandSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new Forest(), new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void missingTargetPreventsLandSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirrorBox());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
        harness.assertInGraveyard(player1, "Boseiju, Who Endures");
    }

    @Test
    void opponentCanDeclineSearchingWithoutShuffling() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirrorBox());
        List<Card> library = List.of(new Forest(), new BoseijuWhoEndures(), new JukaiNaturalist());
        harness.setHand(player1, List.of(new BoseijuWhoEndures()));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mirror Box");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertNotOnBattlefield(player2, "Forest");
    }
}
