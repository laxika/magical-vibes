package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Counterpoint.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class,
        Cancel.class, Banefire.class, BeanstalkGiant.class, GarrukWildspeaker.class,
        HangarbackWalker.class, SolRing.class, VillageRites.class})
class CounterpointTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and may cast an eligible lower-mana-value instant from the graveyard for free")
    void countersSpellAndCastsInstantFromGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");

        harness.setHand(player1, List.of(new Counterpoint()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Counsel of the Soratami");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Offers a creature card with mana value equal to the countered spell")
    void offersCreatureCardAtTheManaValueLimit() {
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        harness.setHand(player1, List.of(new Counterpoint()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, gd.stack.getFirst().getCard().getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer a graveyard card above the countered spell's mana value")
    void rejectsHigherManaValueGraveyardCard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new Counterpoint()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
    }

    @Test
    void mayDeclineTheFreeCast() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        counterSpell(gd.stack.getFirst().getCard().getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castsSorceryDuringOpponentsTurnWithoutPayingMana() {
        harness.setGraveyard(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");

        counterSpell(gd.stack.getFirst().getCard().getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Counsel of the Soratami");
    }

    @Test
    void choosesOnlyOneOfMultipleEligibleGraveyardCards() {
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");

        counterSpell(gd.stack.getFirst().getCard().getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotCastCardsFromOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");

        counterSpell(gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotOfferAnArtifactWithoutAnEligibleSpellType() {
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        counterSpell(gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Sol Ring");
    }

    @Test
    void mayRecastItsOwnCounteredTargetFromTheGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        counterSpell(gd.stack.getFirst().getCard().getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void doesNotCastFromGraveyardWhenTargetSpellBecomesIllegal() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");
        UUID targetId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new Counterpoint()));
        addCounterpointMana();
        harness.castInstant(player1, 0, targetId);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void stillCastsPlaneswalkerWhenTargetCannotBeCountered() {
        harness.setGraveyard(player1, List.of(new GarrukWildspeaker()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Banefire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castSorcery(player2, 0, 5, player1.getId());
        UUID banefireId = gd.stack.getFirst().getCard().getId();

        counterSpell(banefireId);
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getId()).isEqualTo(banefireId));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.passBothPriorities();
        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player2, "Banefire");
    }

    @Test
    void countsEveryXSymbolInTargetSpellManaValue() {
        harness.setGraveyard(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HangarbackWalker()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player2, 0, 2, null, null);

        counterSpell(gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Hangarback Walker");
    }

    @Test
    void offersAdventureWithEligibleManaValueEvenWhenCreatureFaceIsTooExpensive() {
        harness.setGraveyard(player1, List.of(new BeanstalkGiant()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CounselOfTheSoratami(), "{2}{U}");

        counterSpell(gd.stack.getFirst().getCard().getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCastWithAdventure()).isTrue();
        harness.assertNotInGraveyard(player1, "Beanstalk Giant");
    }

    @Test
    void cannotCastSpellWithoutPayingItsMandatorySacrificeCost() {
        VillageRites rites = new VillageRites();
        harness.setGraveyard(player1, List.of(rites));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        counterSpell(gd.stack.getFirst().getCard().getId());
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Village Rites");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(rites.getId()));
    }

    private void counterSpell(UUID targetId) {
        harness.setHand(player1, List.of(new Counterpoint()));
        addCounterpointMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addCounterpointMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
