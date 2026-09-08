package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.StalkingTiger;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestlessDead.class, StalkingTiger.class})
class RestlessDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingAbilityGrantsShield() {
        addCreatureReady(player1, new RestlessDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RestlessDead());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Restless Dead from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent dead = addCreatureReady(player1, new RestlessDead());
        dead.setRegenerationShield(1);

        Permanent attacker = addCreatureReady(player2, new StalkingTiger());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Restless Dead");
        Permanent survivor = findPermanent(player1, "Restless Dead");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Without a regeneration shield Restless Dead dies in combat")
    void diesWithoutShield() {
        addCreatureReady(player1, new RestlessDead());

        Permanent attacker = addCreatureReady(player2, new StalkingTiger());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Restless Dead");
        harness.assertInGraveyard(player1, "Restless Dead");
    }

}
