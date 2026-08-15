package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrsCauseTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices a creature and puts the ability on the stack")
    void activatingSacrificesCreature() {
        addCause(player1);
        Permanent fodder = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsNextDamageFromChosenSource() {
        harness.setLife(player2, 20);
        addCause(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent pyromancer = addReadyCreature(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        addCause(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCause(Player player) {
        return addPermanent(player, new MartyrsCause());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = addPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
