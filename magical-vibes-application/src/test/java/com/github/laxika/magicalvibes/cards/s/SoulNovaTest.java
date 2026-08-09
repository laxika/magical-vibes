package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulNovaTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the attacking creature and all Equipment attached to it")
    void exilesAttackerAndAttachedEquipment() {
        Permanent attacker = addAttacker(player1);

        Permanent playerEquipment = new Permanent(new LeoninScimitar());
        playerEquipment.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(playerEquipment);

        Permanent opponentEquipment = new Permanent(new LeoninScimitar());
        opponentEquipment.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player2.getId()).add(opponentEquipment);

        Permanent unrelatedEquipment = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player2.getId()).add(unrelatedEquipment);

        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        castSoulNova(attacker.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gameData.exiledCards)
                .filteredOn(exiled -> exiled.card().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gameData.exiledCards)
                .filteredOn(exiled -> exiled.card().getName().equals("Leonin Scimitar"))
                .hasSize(2);
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Pacifism");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent attacker = addAttacker(player1);
        Permanent nonAttacker = new Permanent(new GrizzlyBears());
        nonAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(nonAttacker);

        prepareSoulNova();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(attacker, nonAttacker);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    private void castSoulNova(UUID targetId) {
        prepareSoulNova();
        harness.castInstant(player2, 0, targetId);
    }

    private void prepareSoulNova() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SoulNova()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
    }
}
