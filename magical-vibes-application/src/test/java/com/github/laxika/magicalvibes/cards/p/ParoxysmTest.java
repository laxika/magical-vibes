package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParoxysmTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature with Paroxysm")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Paroxysm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent with Paroxysm")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent artifact = findPermanentOnBattlefield(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new Paroxysm()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("A revealed land destroys the enchanted creature")
    void landRevealDestroysCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachParoxysm(creature);
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A revealed nonland card gives the enchanted creature +3/+3 until end of turn")
    void nonlandRevealBoostsCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachParoxysm(creature);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger reveals the enchanted creature controller's library")
    void triggerUsesEnchantedCreatureControllersLibrary() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachParoxysm(creature);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void attachParoxysm(Permanent creature) {
        Permanent aura = new Permanent(new Paroxysm());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private Permanent findPermanentOnBattlefield(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
