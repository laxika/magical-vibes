package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LavaCoil;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
