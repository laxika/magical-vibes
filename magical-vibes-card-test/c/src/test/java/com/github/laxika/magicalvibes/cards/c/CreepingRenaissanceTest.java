package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.g.GarrukRelentless;
import com.github.laxika.magicalvibes.cards.g.GeistcatchersRig;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepingRenaissance.class, AncientGrudge.class, AmbushViper.class,
        TravelersAmulet.class, IntangibleVirtue.class, Mountain.class, GarrukRelentless.class,
        GeistcatchersRig.class, InvasionOfZendikar.class})
class CreepingRenaissanceTest extends BaseCardTest {

    private void castCreepingRenaissance() {
        harness.setHand(player1, List.of(new CreepingRenaissance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Choosing CREATURE returns all creature cards from graveyard to hand")
    void choosingCreatureReturnsAllCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card bear1 = new AmbushViper();
        Card bear2 = new AmbushViper();
        harness.setGraveyard(player1, List.of(bear1, bear2));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(bear1, bear2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(bear1, bear2);
    }

    @Test
    @DisplayName("Choosing ARTIFACT returns all artifact cards from graveyard to hand")
    void choosingArtifactReturnsAllArtifacts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new TravelersAmulet();
        harness.setGraveyard(player1, List.of(artifact));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(artifact);
    }

    @Test
    @DisplayName("Choosing ENCHANTMENT returns all enchantment cards from graveyard to hand")
    void choosingEnchantmentReturnsAllEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new IntangibleVirtue();
        harness.setGraveyard(player1, List.of(enchantment));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "ENCHANTMENT");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Choosing LAND returns all land cards from graveyard to hand")
    void choosingLandReturnsAllLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(land));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(land);
    }

    @Test
    @DisplayName("Non-matching card types remain in the graveyard")
    void nonMatchingTypesStayInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new AmbushViper();
        Card artifact = new TravelersAmulet();
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(creature, artifact, land));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(creature)
                .doesNotContain(artifact, land);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(artifact, land)
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Works with empty graveyard — no error, no cards returned")
    void worksWithEmptyGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        // Only the spell itself should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Creeping Renaissance"));
    }

    @Test
    @DisplayName("Choosing a type with no matching cards in graveyard returns nothing")
    void noMatchingCardsReturnsNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new AmbushViper();
        harness.setGraveyard(player1, List.of(creature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .anyMatch(c -> c.getName().equals("Creeping Renaissance"));
    }

    @Test
    @DisplayName("Flashback returns chosen type cards to hand and exiles spell")
    void flashbackReturnsCardsAndExilesSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new AmbushViper();
        harness.setGraveyard(player1, List.of(new CreepingRenaissance(), creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(creature);
        harness.assertNotInGraveyard(player1, "Creeping Renaissance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Creeping Renaissance"));
    }

    @Test
    @DisplayName("Does not return cards from opponent's graveyard")
    void doesNotReturnFromOpponentGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCreature = new AmbushViper();
        harness.setGraveyard(player2, List.of(opponentCreature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not return instant or sorcery cards even with CREATURE choice")
    void doesNotReturnNonPermanentCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card instant = new AncientGrudge();
        Card creature = new AmbushViper();
        harness.setGraveyard(player1, List.of(instant, creature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(creature)
                .doesNotContain(instant);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(instant);
    }

    @Test
    @DisplayName("Choosing PLANESWALKER returns planeswalkers without affecting creatures")
    void choosingPlaneswalkerReturnsPlaneswalkers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card planeswalker = new GarrukRelentless();
        Card creature = new AmbushViper();
        harness.setGraveyard(player1, List.of(planeswalker, creature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "PLANESWALKER");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(planeswalker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).doesNotContain(planeswalker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "CREATURE"})
    @DisplayName("An artifact creature is returned for either of its permanent types")
    void returnsMultitypeCardForEitherType(String chosenType) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card artifactCreature = new GeistcatchersRig();
        harness.setGraveyard(player1, List.of(artifactCreature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, chosenType);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifactCreature);
    }

    @Test
    @DisplayName("Battle is offered as a permanent type and returns battle cards")
    void choosingBattleReturnsBattles() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card battle = new InvasionOfZendikar();
        harness.setGraveyard(player1, List.of(battle));

        castCreepingRenaissance();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .contains("BATTLE");
        harness.handleListChoice(player1, "BATTLE");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(battle);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(battle);
    }

    @Test
    @DisplayName("A battle with a creature back face is not a creature card in the graveyard")
    void creatureChoiceDoesNotReturnBattleWithCreatureBackFace() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card battle = new InvasionOfZendikar();
        Card creature = new AmbushViper();
        harness.setGraveyard(player1, List.of(battle, creature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(battle).doesNotContain(creature);
    }
}
