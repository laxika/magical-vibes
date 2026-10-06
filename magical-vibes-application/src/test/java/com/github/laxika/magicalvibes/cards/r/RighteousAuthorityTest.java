package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusKeyrune;
import com.github.laxika.magicalvibes.cards.c.CyclonicRift;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RighteousAuthority.class, DrudgeBeetle.class, AzoriusKeyrune.class, CyclonicRift.class})
class RighteousAuthorityTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Casting Righteous Authority puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        harness.setHand(player1, List.of(new RighteousAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(RighteousAuthority.class);
    }

    @Test
    @DisplayName("Resolving attaches and grants +1/+1 per card in enchanted controller's hand")
    void resolvesAndBoostsPerHandCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        // Empty the starting hand so the boost is deterministic after casting.
        gd.playerHands.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new RighteousAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Aura left hand; hand is empty, so the bonus is +0/+0.
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Put three cards in hand for a +3/+3 bonus.
        harness.setHand(player1, List.of(new DrudgeBeetle(), new DrudgeBeetle(), new DrudgeBeetle()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost tracks the enchanted creature's controller's hand, not the Aura's")
    void boostUsesEnchantedControllerHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());

        attachAura(bears);

        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        harness.setHand(player1, List.of(new DrudgeBeetle(), new DrudgeBeetle())); // Aura controller: 2
        harness.setHand(player2, List.of(new DrudgeBeetle(), new DrudgeBeetle(), new DrudgeBeetle())); // host: 3

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Enchanted controller draws an additional card at their draw step")
    void drawsExtraCardAtEnchantedControllerDrawStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        attachAura(bears);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        // Normal draw + Authority's additional draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Does not draw on the Aura controller's draw step when host is controlled by the opponent")
    void doesNotDrawOnAuraControllerTurnWhenHostIsOpponents() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        attachAura(bears);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        // Only the normal draw for player1; Authority does not trigger.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore);
    }

    @Test
    @DisplayName("Draws for the enchanted controller even when the Aura is controlled by the opponent")
    void drawsForEnchantedControllerNotAuraController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        attachAura(bears);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.addToBattlefield(player1, new AzoriusKeyrune());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RighteousAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent artifact = findPermanent(player1, "Azorius Keyrune");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bonus decreases when cards leave the enchanted controller's hand")
    void bonusDecreasesWithHandSize() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        attachAura(creature);
        harness.setHand(player1, List.of(new DrudgeBeetle(), new DrudgeBeetle()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.setHand(player1, List.of(new DrudgeBeetle()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Authorities each grant their bonus and an additional draw")
    void multipleAuthoritiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        attachAura(creature);
        attachAura(creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DrudgeBeetle(), new DrudgeBeetle(),
                new DrudgeBeetle(), new DrudgeBeetle()));

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("The additional draw still resolves after the Aura leaves the battlefield")
    void drawTriggerSurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        attachAura(creature);
        Permanent aura = findPermanent(player1, "Righteous Authority");
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CyclonicRift()));
        harness.setLibrary(player2, List.of(new DrudgeBeetle(), new DrudgeBeetle(), new DrudgeBeetle()));

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RighteousAuthority());
        aura.setAttachedTo(creature.getId());
    }
}
