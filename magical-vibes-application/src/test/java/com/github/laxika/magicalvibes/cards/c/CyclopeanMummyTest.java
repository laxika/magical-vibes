package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FeldonsCane;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclopeanMummy.class, WrathOfGod.class, FeldonsCane.class, Millstone.class})
class CyclopeanMummyTest extends BaseCardTest {

    @Test
    @DisplayName("When Cyclopean Mummy dies, it is exiled instead of staying in the graveyard")
    void diesGoesToExile() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new CyclopeanMummy());
        Card mummyCard = mummy.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(mummyCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(mummyCard.getId()));
    }

    @Test
    @DisplayName("The mummy enters the graveyard before its exile trigger resolves")
    void deathTriggerUsesTheStack() {
        harness.addToBattlefield(player1, new CyclopeanMummy());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cyclopean Mummy");
        harness.assertInGraveyard(player1, "Cyclopean Mummy");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertNotInGraveyard(player1, "Cyclopean Mummy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cyclopean Mummy"));
    }

    @Test
    @DisplayName("The death trigger cannot exile the mummy after it moves to the library")
    void doesNotFollowCardOutOfGraveyard() {
        Card mummy = new CyclopeanMummy();
        harness.addToBattlefield(player1, new FeldonsCane());
        harness.addToBattlefield(player1, mummy);
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mummy.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(mummy.getId()));
    }

    @Test
    @DisplayName("The death trigger cannot exile a mummy that left and reentered the graveyard")
    void doesNotExileNewGraveyardObject() {
        Card mummy = new CyclopeanMummy();
        harness.addToBattlefield(player1, new FeldonsCane());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, mummy);
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Cyclopean Mummy");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Cyclopean Mummy");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Cyclopean Mummy");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mummy.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(mummy.getId()));
    }
}
