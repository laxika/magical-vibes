package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnzragTheQuakeMole.class, GrizzlyBears.class})
class AnzragTheQuakeMoleTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps each creature you control and grants an additional combat")
    void becomingBlockedUntapsCreaturesAndGrantsAdditionalCombat() {
        Permanent anzrag = addReady(player1, new AnzragTheQuakeMole());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        anzrag.setAttacking(true);
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(anzrag.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability makes Anzrag must be blocked this turn")
    void activatedAbilityMakesSourceMustBeBlocked() {
        harness.addToBattlefield(player1, new AnzragTheQuakeMole());
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent anzrag = findPermanent(player1, "Anzrag, the Quake-Mole");
        assertThat(anzrag.isMustBeBlockedThisTurn()).isTrue();

        anzrag.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
    }

    @Test
    @DisplayName("Activated must-be-blocked requirement wears off at end of turn")
    void activatedRequirementWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AnzragTheQuakeMole());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent anzrag = findPermanent(player1, "Anzrag, the Quake-Mole");
        assertThat(anzrag.isMustBeBlockedThisTurn()).isFalse();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
