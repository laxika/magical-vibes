package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistbladeShinobi.class, GnarledMass.class, TendoIceBridge.class})
class MistbladeShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player lets the controller bounce a creature that player controls")
    void bouncesCreature() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GnarledMass());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInHand(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("The bounce is optional — returning nothing is allowed")
    void bounceIsOptional() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        addCreatureReady(player2, new GnarledMass());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Only one creature may be returned even when the Shinobi is pumped")
    void returnsOnlyOneCreatureRegardlessOfDamage() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        shinobi.setPowerModifier(3);
        addCreatureReady(player2, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("No choice when the damaged player controls no creatures")
    void noChoiceWithoutCreatures() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked Shinobi deals no damage to the player and does not trigger")
    void noTriggerWhenBlocked() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GnarledMass());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Only a creature controlled by the damaged player is offered for the optional bounce")
    void onlyCreaturesCanBeReturned() {
        Permanent shinobi = addCreatureReady(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        harness.addToBattlefieldAndReturn(player2, new TendoIceBridge());

        resolveCombat();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player2, "Gnarled Mass");
        harness.assertOnBattlefield(player2, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Mistblade Shinobi onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.setHand(player1, List.of(new MistbladeShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent shinobi = findPermanent(player1, "Mistblade Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }
}
