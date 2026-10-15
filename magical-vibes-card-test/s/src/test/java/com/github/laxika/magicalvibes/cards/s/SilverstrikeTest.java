package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Silverstrike.class, DevilthornFox.class, SurviveTheNight.class})
class SilverstrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking creature and gains 3 life")
    void destroysAttackerAndGainsLife() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);

        castSilverstrike(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Silverstrike()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles without gaining life if the target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);

        castSilverstrike(attacker.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Silverstrike");
    }

    @Test
    @DisplayName("Does not resolve if the target stops attacking")
    void doesNotResolveIfTargetStopsAttacking() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);

        castSilverstrike(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devilthorn Fox");
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Silverstrike");
    }

    @Test
    @DisplayName("Gains life even when the attacking creature is indestructible")
    void gainsLifeWhenDestructionIsPrevented() {
        harness.setLife(player2, 15);
        Permanent attacker = addAttacker(player1);
        castSilverstrike(attacker.getId());

        harness.setHand(player1, List.of(new SurviveTheNight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devilthorn Fox");
        harness.assertNotInGraveyard(player1, "Devilthorn Fox");
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Silverstrike");
    }

    @Test
    @DisplayName("Can destroy its controller's own attacking creature")
    void canTargetOwnAttackingCreature() {
        harness.setLife(player1, 15);
        Permanent attacker = addAttacker(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new Silverstrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertLife(player1, 18);
    }

    private void castSilverstrike(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Silverstrike()));
        addMana();
        harness.castInstant(player2, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, new DevilthornFox());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
