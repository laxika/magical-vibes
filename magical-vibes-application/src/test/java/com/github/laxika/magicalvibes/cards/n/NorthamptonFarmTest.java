package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthamptonFarm.class, Forest.class, GrizzlyBears.class})
class NorthamptonFarmTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapsForColorless() {
        harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature you own and tracks it with Northampton Farm")
    void exilesOwnedCreature() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(farm.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot exile a creature you do not own")
    void cannotExileUnownedCreature() {
        harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing the Farm returns one exiled creature and other exiled cards to their owners' hands")
    void sacrificeReturnsCreatureAndOtherCards() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Card otherCard = new Forest();
        gd.addToExile(player1.getId(), otherCard, farm.getId());
        farm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Northampton Farm");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getCardsExiledByPermanent(farm.getId())).isEmpty();
    }
}
