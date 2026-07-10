package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroundskeeperTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{G}: Return target basic land card from graveyard to hand")
    void returnBasicLandFromGraveyard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a non-basic-land card")
    void cannotReturnNonBasicLand() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, elves.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
