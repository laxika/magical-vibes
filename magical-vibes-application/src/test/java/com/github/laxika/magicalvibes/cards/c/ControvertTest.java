package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Controvert.class, BorealDruid.class, BorealShelf.class})
class ControvertTest extends BaseCardTest {

    @Test
    void countersTargetSpell() {
        BorealDruid druid = new BorealDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Controvert()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, druid.getId());

        harness.assertInGraveyard(player1, "Boreal Druid");
        harness.assertInGraveyard(player2, "Controvert");
    }

    @Test
    void recoverReturnsControvertToHandWhenPaid() {
        Card controvert = new Controvert();
        harness.setGraveyard(player1, List.of(controvert));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(controvert);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(controvert);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(controvert);
    }

    @Test
    void recoverExilesControvertWhenDeclined() {
        Card controvert = new Controvert();
        harness.setGraveyard(player1, List.of(controvert));
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controvert);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(controvert);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(controvert);
    }

    @Test
    void recoverDoesNotTriggerForOpponentCreature() {
        Card controvert = new Controvert();
        harness.setGraveyard(player1, List.of(controvert));
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controvert);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controvert);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(controvert);
    }

    @Test
    void recoverDoesNotTriggerForNoncreature() {
        Card controvert = new Controvert();
        harness.setGraveyard(player1, List.of(controvert));
        Permanent shelf = harness.addToBattlefieldAndReturn(player1, new BorealShelf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shelf));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controvert);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controvert);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(controvert);
    }
}
