package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreepingRenaissanceTest extends BaseCardTest {

    private void castCreepingRenaissance() {
        harness.setHand(player1, new ArrayList<>(List.of(new CreepingRenaissance())));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Choosing CREATURE returns all creature cards from graveyard to hand")
    void choosingCreatureReturnsAllCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(bear1, bear2));

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

        Card artifact = new DarksteelRelic();
        gd.playerGraveyards.get(player1.getId()).add(artifact);

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

        Card enchantment = new GloriousAnthem();
        gd.playerGraveyards.get(player1.getId()).add(enchantment);

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
        gd.playerGraveyards.get(player1.getId()).add(land);

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

        Card creature = new GrizzlyBears();
        Card artifact = new DarksteelRelic();
        Card land = new Mountain();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(creature, artifact, land));

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

        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(creature);

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

        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new CreepingRenaissance(), creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Creeping Renaissance"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Creeping Renaissance"));
    }

    @Test
    @DisplayName("Does not return cards from opponent's graveyard")
    void doesNotReturnFromOpponentGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCreature = new GrizzlyBears();
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature);

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
        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(instant, creature));

        castCreepingRenaissance();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(creature)
                .doesNotContain(instant);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(instant);
    }
}
