package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Remove.class, BearCub.class})
class RemoveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target attacking creature to its owner's hand")
    void returnsAttacker() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new Remove()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, List.of(a1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(a1);
        assertThat(gd.playerHands.get(player1.getId())).contains(a1.getCard());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent idle = idleCreature(player2);
        harness.setHand(player2, List.of(new Remove()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(idle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addAttackerTargeting(player2, player1);
        Remove remove = new Remove();
        harness.setHand(player2, List.of(remove));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttackerTargeting(player1, player2);
        harness.setHand(player2, List.of(new Remove()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(a1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not return the target if it stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        Remove remove = new Remove();
        harness.setHand(player2, List.of(remove));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, List.of(attacker.getId()));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(remove);
    }

    @Test
    @DisplayName("Returns a controlled attacking creature to its owner's hand")
    void returnsAttackerToOwnerHand() {
        harness.forceActivePlayer(player2);
        BearCub attackerCard = new BearCub();
        attackerCard.setOwnerId(player1.getId());
        Permanent attacker = addAttackerTargeting(player2, player1, attackerCard);
        Remove remove = new Remove();
        harness.setHand(player1, List.of(remove));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attackerCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(attackerCard);
    }

    @Test
    @DisplayName("Returns only the chosen attacker after another attacker leaves combat")
    void returnsOnlyChosenAttacker() {
        Permanent first = addCreatureReady(player1, new BearCub());
        Permanent second = addCreatureReady(player1, new BearCub());
        Remove remove = new Remove();
        harness.setHand(player2, List.of(remove));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));
        first.setAttacking(false);

        harness.castAndResolveInstant(player2, 0, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerHands.get(player1.getId())).contains(second.getCard()).doesNotContain(first.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(remove);
    }

    @Test
    @DisplayName("Does not return a target that left the battlefield before resolution")
    void fizzlesIfTargetLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        Remove remove = new Remove();
        harness.setHand(player2, List.of(remove));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(remove);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        return addAttackerTargeting(attackerController, defender, new BearCub());
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender,
                                           BearCub card) {
        Permanent perm = addCreatureReady(attackerController, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent idleCreature(Player player) {
        return addCreatureReady(player, new BearCub());
    }
}
