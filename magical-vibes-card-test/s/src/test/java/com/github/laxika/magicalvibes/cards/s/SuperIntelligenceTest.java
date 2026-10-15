package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperIntelligence.class, GrizzlyBears.class, FountainOfYouth.class, TrueBeliever.class})
class SuperIntelligenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Super Intelligence attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuperIntelligence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Super Intelligence")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new SuperIntelligence()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature's controller draws a card at their upkeep")
    void enchantedCreatureControllerDrawsAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 1);
    }

    @Test
    @DisplayName("Does not draw at the Aura controller's upkeep")
    void doesNotDrawAtAuraControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SuperIntelligence());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanting your own creature draws exactly one card during your upkeep")
    void drawsForOwnCreatureController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Super Intelligence draws a card for the enchanted creature's controller")
    void multipleAurasEachDrawOneCard() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);
        attachAura(creature);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing the Aura after its upkeep ability triggers does not prevent the draw")
    void pendingTriggerSurvivesAuraLeavingBattlefield() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        Permanent aura = findPermanent(player1, "Super Intelligence");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Player shroud does not prevent the nontargeting upkeep draw")
    void playerShroudDoesNotPreventDraw() {
        Permanent creature = addCreatureReady(player2, new TrueBeliever());
        attachAura(creature);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }
}
