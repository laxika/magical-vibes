package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarbaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying upkeep keeps Darba on the battlefield")
    void payingUpkeepKeepsDarba() {
        Permanent darba = harness.addToBattlefieldAndReturn(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(darba);
    }

    @Test
    @DisplayName("Declining upkeep sacrifices Darba")
    void decliningUpkeepSacrificesDarba() {
        harness.addToBattlefieldAndReturn(player1, new Darba());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Darba");
    }
}
