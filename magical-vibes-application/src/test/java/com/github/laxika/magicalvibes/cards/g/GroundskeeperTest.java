package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DustBowl;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Groundskeeper.class, Forest.class, DustBowl.class})
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
    @DisplayName("Cannot target a nonbasic land")
    void cannotReturnNonbasicLand() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card dustBowl = new DustBowl();
        harness.setGraveyard(player1, List.of(dustBowl));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, dustBowl.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a basic land in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
