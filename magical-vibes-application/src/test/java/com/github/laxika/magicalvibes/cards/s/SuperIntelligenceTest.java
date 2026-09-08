package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperIntelligence.class, GrizzlyBears.class, FountainOfYouth.class})
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
        Permanent aura = new Permanent(new SuperIntelligence());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
