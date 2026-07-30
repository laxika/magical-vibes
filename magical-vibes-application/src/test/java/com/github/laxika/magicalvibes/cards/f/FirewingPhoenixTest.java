package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirewingPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the graveyard ability puts it on the stack")
    void activatingGraveyardAbilityUsesStack() {
        harness.setGraveyard(player1, List.of(new FirewingPhoenix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Firewing Phoenix");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving the graveyard ability returns Firewing Phoenix to hand")
    void resolvingGraveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new FirewingPhoenix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Firewing Phoenix");
        harness.assertNotInGraveyard(player1, "Firewing Phoenix");
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability without three red mana")
    void cannotActivateWithoutEnoughRedMana() {
        harness.setGraveyard(player1, List.of(new FirewingPhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
