package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpareSupplies.class, Forest.class})
class SpareSuppliesTest extends BaseCardTest {

    @Test
    @DisplayName("Spare Supplies enters tapped and draws a card")
    void entersTappedAndDrawsCard() {
        harness.setHand(player1, List.of(new SpareSupplies()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent supplies = findPermanent(player1, "Spare Supplies");
        assertThat(supplies.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Paying two mana and tapping Spare Supplies sacrifices it and draws a card")
    void sacrificesAndDrawsCard() {
        Permanent supplies = new Permanent(new SpareSupplies());
        supplies.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(supplies);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spare Supplies");
        harness.assertInGraveyard(player1, "Spare Supplies");
        harness.assertInHand(player1, "Forest");
    }
}
