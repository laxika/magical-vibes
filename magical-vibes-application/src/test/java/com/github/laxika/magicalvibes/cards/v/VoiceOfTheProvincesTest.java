package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoiceOfTheProvincesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Voice of the Provinces puts its ETB token trigger on the stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new VoiceOfTheProvinces()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voice of the Provinces");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB trigger creates a 1/1 white Human token")
    void etbCreatesHumanToken() {
        harness.setHand(player1, List.of(new VoiceOfTheProvinces()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
    }
}
