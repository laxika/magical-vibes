package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilloryOfTheSleepless.class, GrizzlyBears.class, FountainOfYouth.class})
class PilloryOfTheSleeplessTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Controller of enchanted creature loses 1 life at upkeep")
    void enchantedCreatureControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new PilloryOfTheSleepless()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new PilloryOfTheSleepless());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }
}
