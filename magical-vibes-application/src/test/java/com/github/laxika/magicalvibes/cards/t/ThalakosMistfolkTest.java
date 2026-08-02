package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThalakosMistfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} puts Thalakos Mistfolk on top of its owner's library")
    void activateAbilityPutsOnTopOfLibrary() {
        harness.addToBattlefieldAndReturn(player1, new ThalakosMistfolk()).setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thalakos Mistfolk");
        harness.assertNotInHand(player1, "Thalakos Mistfolk");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Thalakos Mistfolk");
    }

    @Test
    @DisplayName("Ability can be activated the turn Thalakos Mistfolk enters (no tap cost)")
    void abilityUsableWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new ThalakosMistfolk()).setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Thalakos Mistfolk");
    }
}
