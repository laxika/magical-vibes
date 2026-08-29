package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChildOfGaeaTest extends BaseCardTest {

    @Test
    @DisplayName("Child of Gaea survives its upkeep when its controller pays {G}{G}")
    void survivesUpkeepWhenPaymentIsMade() {
        Permanent child = addChild(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(child);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Child of Gaea is sacrificed when its controller declines the upkeep payment")
    void isSacrificedWhenUpkeepPaymentIsDeclined() {
        Permanent child = addChild(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(child.getCard().getId()));
    }

    @Test
    @DisplayName("Child of Gaea's regeneration ability creates a regeneration shield")
    void regenerationAbilityCreatesShield() {
        Permanent child = addChild(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(child.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addChild(Player player) {
        Permanent child = new Permanent(new ChildOfGaea());
        child.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(child);
        return child;
    }
}
