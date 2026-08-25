package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSoulStone.class, GrizzlyBears.class})
class TheSoulStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one black mana")
    void tapsForBlackMana() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not return a creature before it is harnessed")
    void doesNotReturnCreatureBeforeHarnessed() {
        harness.addToBattlefield(player1, new TheSoulStone());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Harnessing returns a target creature from the graveyard at upkeep")
    void harnessingReturnsCreatureAtUpkeep() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        Permanent creatureToExile = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears creatureToReturn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureToReturn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureToExile.getId());
        harness.passBothPriorities();

        assertThat(stone.isHarnessed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creatureToExile);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creatureToReturn.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureToReturn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creatureToReturn.getId()));
    }
}
