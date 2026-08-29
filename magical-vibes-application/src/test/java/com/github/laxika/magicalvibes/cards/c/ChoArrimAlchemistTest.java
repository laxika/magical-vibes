package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChoArrimAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage from the chosen source and gains that much life")
    void preventsNextDamageAndGainsLife() {
        harness.setLife(player1, 20);
        addReadyAlchemist(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.setHand(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        harness.setLife(player1, 20);
        addReadyAlchemist(player1);
        Permanent chosenSource = addReadyGoblin(player2);
        Permanent otherSource = addReadyGoblin(player2);
        harness.setHand(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosenSource.getId()));
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutDiscard() {
        addReadyAlchemist(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAlchemist(Player player) {
        Permanent alchemist = new Permanent(new ChoArrimAlchemist());
        alchemist.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(alchemist);
        return alchemist;
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent goblin = new Permanent(new GoblinPiker());
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(goblin);
        return goblin;
    }
}
