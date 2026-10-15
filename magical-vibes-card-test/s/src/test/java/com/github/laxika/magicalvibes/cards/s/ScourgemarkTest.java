package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.UnknownShores;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scourgemark.class, TravelingPhilosopher.class, UnknownShores.class})
class ScourgemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0")
    void enchantedCreatureGetsPowerBoost() {
        Permanent bears = attachScourgemark();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Scourgemark draws a card when it enters")
    void drawsCardOnEnter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Scourgemark()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast);
    }

    @Test
    @DisplayName("Scourgemark's boost is lost when it leaves the battlefield")
    void boostLostWhenAuraLeaves() {
        Permanent bears = attachScourgemark();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Scourgemark"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new TravelingPhilosopher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UnknownShores());

        harness.setHand(player1, List.of(new Scourgemark()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws only for the Aura controller")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new Scourgemark()));
        harness.setHand(player2, List.of());
        UnknownShores drawnCard = new UnknownShores();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Scourgemark && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("An illegal target prevents the Aura from entering and drawing")
    void removedTargetPreventsDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new Scourgemark()));
        UnknownShores libraryCard = new UnknownShores();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c instanceof Scourgemark);
    }

    @Test
    @DisplayName("The draw trigger survives removal of the Aura and its enchanted creature")
    void drawTriggerSurvivesRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new Scourgemark()));
        UnknownShores drawnCard = new UnknownShores();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Scourgemark");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent attachScourgemark() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Scourgemark());
        aura.setAttachedTo(bears.getId());

        return bears;
    }
}
