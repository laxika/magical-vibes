package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroatSlitter.class, GnarledMass.class, BileUrchin.class})
class ThroatSlitterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player destroys a chosen nonblack creature that player controls")
    void destroysChosenNonblackCreature() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GnarledMass());

        resolveCombat();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Black creatures and the attacker's own creatures are not valid choices")
    void onlyDamagedPlayersNonblackCreatures() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GnarledMass());
        Permanent enemyCreature = addCreatureReady(player2, new GnarledMass());
        Permanent enemyBlackCreature = addCreatureReady(player2, new BileUrchin());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyCreature.getId())
                .doesNotContain(ownCreature.getId())
                .doesNotContain(enemyBlackCreature.getId());
    }

    @Test
    @DisplayName("No target choice when the damaged player controls only black creatures")
    void noTargetChoiceWithoutNonblackCreatures() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        addCreatureReady(player2, new BileUrchin());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Bile Urchin");
    }

    @Test
    @DisplayName("A blocked Throat Slitter that deals no combat damage to a player does not trigger")
    void noTriggerWhenBlocked() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BileUrchin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Throat Slitter onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.setHand(player1, List.of(new ThroatSlitter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent slitter = findPermanent(player1, "Throat Slitter");
        assertThat(slitter.isTapped()).isTrue();
        assertThat(slitter.isAttacking()).isTrue();
        assertThat(slitter.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new BileUrchin());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new ThroatSlitter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }
}
