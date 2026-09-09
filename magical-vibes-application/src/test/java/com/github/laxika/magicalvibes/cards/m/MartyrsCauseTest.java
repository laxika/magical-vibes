package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.ThornwindFaeries;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrsCause.class, GiantCockroach.class, ThornwindFaeries.class})
class MartyrsCauseTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices a creature and puts the ability on the stack")
    void activatingSacrificesCreature() {
        addCause();
        Permanent fodder = addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new ThornwindFaeries());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertNotOnBattlefield(player1, "Giant Cockroach");
        harness.assertInGraveyard(player1, "Giant Cockroach");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsNextDamageFromChosenSource() {
        harness.setLife(player2, 20);
        Permanent cause = addCause();
        Permanent fodder = addCreatureReady(player1, new GiantCockroach());
        Permanent source = addCreatureReady(player1, new ThornwindFaeries());

        harness.activateAbility(player1, indexOf(player1, cause), null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        addCause();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source to a creature")
    void preventsNextDamageToCreature() {
        Permanent cause = addCause();
        Permanent fodder = addCreatureReady(player1, new GiantCockroach());
        Permanent source = addCreatureReady(player1, new ThornwindFaeries());
        Permanent victim = addCreatureReady(player2, new GiantCockroach());

        activateCauseAndChooseSource(cause, fodder, source);

        harness.activateAbility(player1, indexOf(player1, source), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        Permanent cause = addCause();
        Permanent fodder = addCreatureReady(player1, new GiantCockroach());
        Permanent chosenSource = addCreatureReady(player1, new ThornwindFaeries());
        Permanent otherSource = addCreatureReady(player1, new ThornwindFaeries());
        Permanent victim = addCreatureReady(player2, new GiantCockroach());

        activateCauseAndChooseSource(cause, fodder, chosenSource);

        harness.activateAbility(player1, indexOf(player1, otherSource), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(shield -> shield.sourceId())
                .containsExactly(chosenSource.getId());
    }

    private Permanent addCause() {
        return harness.addToBattlefieldAndReturn(player1, new MartyrsCause());
    }

    private void activateCauseAndChooseSource(Permanent cause, Permanent fodder, Permanent source) {
        harness.activateAbility(player1, indexOf(player1, cause), null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
