package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.n.NomadDecoy;
import com.github.laxika.magicalvibes.cards.s.SecondThoughts;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BelovedChaplain.class, DwarvenGrunt.class, NomadDecoy.class, SecondThoughts.class})
class BelovedChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from creatures prevents combat damage")
    void protectionFromCreaturesPreventsCombatDamage() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player2, new BelovedChaplain());
        Permanent attacker = addCreatureReady(player1, new DwarvenGrunt());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(chaplain.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Beloved Chaplain");
        harness.assertInGraveyard(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Protection from creatures prevents creatures from blocking it")
    void protectionFromCreaturesPreventsBeingBlocked() {
        Permanent chaplain = addCreatureReady(player1, new BelovedChaplain());
        chaplain.setAttacking(true);
        addCreatureReady(player2, new DwarvenGrunt());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from creatures prevents creature abilities from targeting it")
    void protectionFromCreaturesPreventsCreatureAbilitiesFromTargetingIt() {
        addCreatureReady(player1, new NomadDecoy());
        Permanent chaplain = harness.addToBattlefieldAndReturn(player2, new BelovedChaplain());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, chaplain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(chaplain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Protection from creatures does not prevent a noncreature spell from targeting it")
    void protectionFromCreaturesDoesNotPreventNoncreatureSpellTargetingIt() {
        Permanent chaplain = addCreatureReady(player1, new BelovedChaplain());
        chaplain.setAttacking(true);
        harness.setHand(player2, List.of(new SecondThoughts()));
        harness.setLibrary(player2, List.of(new DwarvenGrunt()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        harness.castInstant(player2, 0, chaplain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Beloved Chaplain");
    }
}
