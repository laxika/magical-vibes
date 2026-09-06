package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ZoeticCavern.class)
class ZoeticCavernTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new ZoeticCavern());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(cavern.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up as a land")
    void morphsFaceDownAndRestoresLandCharacteristics() {
        harness.setHand(player1, List.of(new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent cavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(cavern.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, cavern)).isTrue();
        assertThat(gqs.isLand(gd, cavern)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cavern));

        assertThat(cavern.isFaceDown()).isFalse();
        assertThat(gqs.isCreature(gd, cavern)).isFalse();
        assertThat(gqs.isLand(gd, cavern)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
