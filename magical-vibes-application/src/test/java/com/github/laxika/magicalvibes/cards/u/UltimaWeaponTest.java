package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltimaWeapon.class, GrizzlyBears.class})
class UltimaWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +7/+7")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = addWeaponReady(player1);
        weapon.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    @DisplayName("Equipped creature's attack destroys a target creature an opponent controls")
    void attackTriggerDestroysOpponentCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = addWeaponReady(player1);
        weapon.setAttachedTo(creature.getId());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Ultima Weapon does not trigger when unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addWeaponReady(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getClass() == UltimaWeapon.class);
    }

    @Test
    @DisplayName("Equip {7} attaches Ultima Weapon to a creature you control")
    void equipAttachesToCreature() {
        Permanent weapon = addWeaponReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(weapon.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addWeaponReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new UltimaWeapon());
    }

    @Test
    @DisplayName("Attack trigger offers only opposing creatures as targets")
    void attackTriggerRestrictsTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = addWeaponReady(player1);
        weapon.setAttachedTo(creature.getId());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addWeaponReady(player2);

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger Ultima Weapon")
    void noTriggerWhenEquippedCreatureDoesNotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = addWeaponReady(player1);
        weapon.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getClass() == UltimaWeapon.class);
    }

    @Test
    @DisplayName("Detaching Ultima Weapon removes its bonus")
    void detachingRemovesBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = addWeaponReady(player1);
        weapon.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);

        weapon.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
