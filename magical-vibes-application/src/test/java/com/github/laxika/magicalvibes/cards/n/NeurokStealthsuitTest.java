package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokStealthsuit.class, Arachnoid.class})
class NeurokStealthsuitTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(suit);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents the blue ability from targeting the equipped creature")
    void shroudPreventsBlueAbilityTargetingEquippedCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        suit.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Blue ability attaches Neurok Stealthsuit at instant speed")
    void blueAbilityAttachesAtInstantSpeed() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Blue ability cannot target a creature controlled by an opponent")
    void blueAbilityRequiresCreatureYouControl() {
        addSuitReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability attaches Neurok Stealthsuit for one mana")
    void equipAbilityAttachesForOneMana() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip ability cannot be activated at instant speed")
    void equipAbilityIsSorcerySpeedOnly() {
        addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip ability cannot target a creature controlled by an opponent")
    void equipAbilityRequiresCreatureYouControl() {
        addSuitReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addSuitReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NeurokStealthsuit());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
