package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LavaCoil;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrapplingSundew.class, LavaCoil.class})
class GrapplingSundewTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants indestructible until end of turn")
    void grantsIndestructibleUntilEndOfTurn() {
        Permanent sundew = harness.addToBattlefieldAndReturn(player1, new GrapplingSundew());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sundew.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sundew.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Indestructible lets Grappling Sundew survive lethal damage")
    void survivesLethalDamageAfterActivation() {
        Permanent sundew = harness.addToBattlefieldAndReturn(player1, new GrapplingSundew());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LavaCoil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, sundew.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grappling Sundew");
        assertThat(sundew.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability uses the stack and only grants indestructible to its source")
    void grantsOnlyToSourceOnResolution() {
        Permanent sundew = harness.addToBattlefieldAndReturn(player1, new GrapplingSundew());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrapplingSundew());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrapplingSundew());
        sundew.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sundew.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(sundew.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(sundew.isTapped()).isTrue();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(opposing.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The ability cannot be activated without green mana")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new GrapplingSundew());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated with only four mana")
    void requiresFiveMana() {
        harness.addToBattlefield(player1, new GrapplingSundew());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
