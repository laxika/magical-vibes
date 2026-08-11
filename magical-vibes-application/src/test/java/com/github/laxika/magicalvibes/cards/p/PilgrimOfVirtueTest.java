package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PilgrimOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices Pilgrim of Virtue")
    void activatingAbilitySacrificesPilgrim() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);

        harness.assertNotOnBattlefield(player1, "Pilgrim of Virtue");
        harness.assertInGraveyard(player1, "Pilgrim of Virtue");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability only allows a black source to be chosen")
    void resolvingAbilityOnlyAllowsBlackSource() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent blackSource = addCreatureReady(player2, new ScatheZombies());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(blackSource.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen black source")
    void preventsNextDamageFromChosenBlackSource() {
        harness.setLife(player1, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent blackSource = addCreatureReady(player2, new ScatheZombies());

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        blackSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A non-black source cannot be chosen")
    void nonBlackSourceCannotBeChosen() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
