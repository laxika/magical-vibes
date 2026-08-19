package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuardDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a permanent on resolution and prevents combat damage from a creature sharing its color")
    void preventsCombatDamageWhenColorsShare() {
        Permanent guardDogs = addReady(player1, new GuardDogs());
        Permanent chosen = addReady(player1, new SerraAngel());
        Permanent attacker = addReady(player2, new SerraAngel());

        addAbilityMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null,
                attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, chosen.getId());

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    @DisplayName("Does not prevent combat damage when the chosen permanent and target have no color in common")
    void doesNotPreventCombatDamageWhenColorsDoNotShare() {
        Permanent guardDogs = addReady(player1, new GuardDogs());
        Permanent chosen = addReady(player1, new Forest());
        Permanent attacker = addReady(player2, new SerraAngel());

        addAbilityMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null,
                attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat - 4);
    }

    @Test
    void cannotTargetANoncreature() {
        Permanent guardDogs = addReady(player1, new GuardDogs());
        Permanent forest = addReady(player2, new Forest());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void attackWithoutBlockers(Permanent attacker) {
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();
    }
}
