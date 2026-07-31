package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelIngotTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and taps Darksteel Ingot")
    void manaAbilityAddsChosenColor() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        ingot.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ingot.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible — Naturalize does not destroy Darksteel Ingot")
    void survivesDestroy() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, ingot.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
    }
}
