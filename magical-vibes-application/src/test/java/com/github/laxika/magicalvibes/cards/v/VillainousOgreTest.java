package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReaperFromTheAbyss;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VillainousOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Villainous Ogre cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addOgre(player2);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Regeneration ability cannot be activated without a Demon")
    void cannotRegenerateWithoutDemon() {
        addOgre(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration ability can be activated while you control a Demon")
    void canRegenerateWithDemon() {
        Permanent ogre = addOgre(player1);
        Permanent demon = new Permanent(new ReaperFromTheAbyss());
        demon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(demon);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(ogre.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Demon controlled by an opponent does not enable the regeneration ability")
    void opponentsDemonDoesNotEnableRegeneration() {
        addOgre(player1);
        Permanent demon = new Permanent(new ReaperFromTheAbyss());
        demon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(demon);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addOgre(com.github.laxika.magicalvibes.model.Player player) {
        Permanent ogre = new Permanent(new VillainousOgre());
        ogre.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ogre);
        return ogre;
    }
}
