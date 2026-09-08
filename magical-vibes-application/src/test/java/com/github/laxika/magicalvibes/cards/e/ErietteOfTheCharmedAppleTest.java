package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErietteOfTheCharmedApple.class, GrizzlyBears.class, HolyStrength.class})
class ErietteOfTheCharmedAppleTest extends BaseCardTest {

    @Test
    @DisplayName("A creature enchanted by an Aura you control cannot attack you")
    void auraEnchantedCreatureCannotAttackController() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, attacker);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An Aura controlled by an opponent does not restrict the creature")
    void auraControlledByOpponentDoesNotRestrictAttack() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, attacker);

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("At your end step, each opponent loses and you gain life for each Aura you control")
    void drainsForEachAuraYouControl() {
        harness.addToBattlefield(player1, new ErietteOfTheCharmedApple());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, firstCreature);
        attachAura(player1, secondCreature);
        attachAura(player2, firstCreature);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The Aura trigger does not happen at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new ErietteOfTheCharmedApple());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void attachAura(Player controller, Permanent host) {
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
