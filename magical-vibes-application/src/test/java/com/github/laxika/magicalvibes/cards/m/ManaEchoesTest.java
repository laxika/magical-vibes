package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaEchoes.class, LlanowarElves.class})
class ManaEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Counts creatures sharing a type at resolution and adds one mana per creature")
    void countsMatchingCreaturesAtResolution() {
        harness.addToBattlefield(player1, new ManaEchoes());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the trigger produces no mana")
    void decliningTriggerProducesNoMana() {
        harness.addToBattlefield(player1, new ManaEchoes());

        harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
