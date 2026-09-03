package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PearlDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FemerefArchers.class, PearlDragon.class, FemerefScouts.class})
class FemerefArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting attacking flying creature puts ability on stack")
    void activatingAbilityPutsOnStack() {
        Permanent archersPerm = addCreatureReady(player1, new FemerefArchers());
        Permanent attacker = addAttackingCreature(player2, new PearlDragon());

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(archersPerm.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Ability deals 4 damage to attacking flying creature and destroys it")
    void abilityDealsFourAndKillsTarget() {
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent attacker = addAttackingCreature(player2, new PearlDragon());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(archers);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(archers.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(attacker.getCard().getId()));
        assertThat(gameLogContains("deals 4 damage")).isTrue();
    }

    @Test
    @DisplayName("Cannot target attacking creature without flying")
    void cannotTargetAttackingCreatureWithoutFlying() {
        addCreatureReady(player1, new FemerefArchers());
        Permanent attacker = addAttackingCreature(player2, new FemerefScouts());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Cannot target flying creature that is not attacking")
    void cannotTargetFlyingCreatureThatIsNotAttacking() {
        addCreatureReady(player1, new FemerefArchers());
        Permanent flyer = addCreatureReady(player2, new PearlDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Cannot activate ability while Femeref Archers has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new FemerefArchers());
        Permanent attacker = addAttackingCreature(player2, new PearlDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        addCreatureReady(player1, new FemerefArchers());
        Permanent attacker = addAttackingCreature(player2, new PearlDragon());

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate the ability while Femeref Archers is already tapped")
    void cannotActivateWhenTapped() {
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        archers.tap();
        Permanent attacker = addAttackingCreature(player2, new PearlDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }
}
