package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshiokWickedManipulator;
import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErietteOfTheCharmedApple.class, GrizzlyBears.class, HolyStrength.class,
        AshiokWickedManipulator.class, DarksteelMutation.class})
class ErietteOfTheCharmedAppleTest extends BaseCardTest {

    @Test
    @DisplayName("A creature enchanted by an Aura you control cannot attack you")
    void auraEnchantedCreatureCannotAttackController() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, attacker);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An Aura controlled by an opponent does not restrict the creature")
    void auraControlledByOpponentDoesNotRestrictAttack() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, attacker);

        declareAttackers(player1, List.of(0));
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

    @Test
    @DisplayName("An unenchanted creature can attack Eriette's controller")
    void unenchantedCreatureCanAttack() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @CardUsed({ErietteOfTheCharmedApple.class, GrizzlyBears.class, HolyStrength.class,
            AshiokWickedManipulator.class})
    @DisplayName("An enchanted creature cannot attack a planeswalker Eriette's controller controls")
    void protectsControllersPlaneswalker() {
        harness.addToBattlefield(player2, new ErietteOfTheCharmedApple());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0),
                Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @CardUsed({ErietteOfTheCharmedApple.class, GrizzlyBears.class, HolyStrength.class,
            DarksteelMutation.class})
    @DisplayName("Eriette's attack restriction stops when she loses her abilities")
    void losingAbilitiesRemovesAttackRestriction() {
        Permanent eriette = harness.addToBattlefieldAndReturn(player2, new ErietteOfTheCharmedApple());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, attacker);
        Permanent mutation = harness.addToBattlefieldAndReturn(player1, new DarksteelMutation());
        mutation.setAttachedTo(eriette.getId());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("With no Auras, the end-step trigger leaves both life totals unchanged")
    void zeroAurasStillTriggers() {
        harness.addToBattlefield(player1, new ErietteOfTheCharmedApple());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Auras on one opposing creature each count for the drain")
    void countsAurasRatherThanEnchantedCreatures() {
        harness.addToBattlefield(player1, new ErietteOfTheCharmedApple());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);
        attachAura(player1, creature);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Auras are counted when the end-step trigger resolves")
    void countsAurasAtResolution() {
        harness.addToBattlefield(player1, new ErietteOfTheCharmedApple());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);
        Permanent removedAura = attachAura(player1, creature);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(removedAura);
        gd.playerGraveyards.get(player1.getId()).add(removedAura.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An end-step trigger still resolves after Eriette leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent eriette = harness.addToBattlefieldAndReturn(player1, new ErietteOfTheCharmedApple());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(eriette);
        gd.playerGraveyards.get(player1.getId()).add(eriette.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent attachAura(Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new HolyStrength());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
