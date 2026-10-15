package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.o.Overprotect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongRiversPull.class, BarkformHarvester.class, Overprotect.class})
class LongRiversPullTest extends BaseCardTest {

    @Test
    void withoutGiftCountersCreatureSpell() {
        BarkformHarvester bears = new BarkformHarvester();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithGift(player2, 0, bears.getId(), false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barkform Harvester");
        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void withoutGiftCannotTargetNoncreatureSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Overprotect might = new Overprotect();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithGift(player2, 0, might.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature spell");
    }

    @Test
    void promisedGiftCountersAnySpellAndDrawsCardForOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Overprotect might = new Overprotect();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        int handSizeBeforeGift = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithGift(player2, 0, might.getId(), true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Overprotect");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeGift + 1);
    }

    @Test
    void promisedGiftAlsoCountersCreatureSpell() {
        BarkformHarvester creature = new BarkformHarvester();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Overprotect()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithGift(player2, 0, creature.getId(), true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barkform Harvester");
        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        harness.assertInHand(player1, "Overprotect");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void promisedGiftDoesNotDrawWhenTargetHasAlreadyBeenCountered() {
        BarkformHarvester creature = new BarkformHarvester();
        harness.setHand(player1, List.of(creature, new LongRiversPull()));
        harness.setLibrary(player1, List.of(new Overprotect()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithGift(player2, 0, creature.getId(), true);
        harness.passPriority(player2);
        harness.castInstantWithGift(player1, 0, creature.getId(), false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barkform Harvester");
        harness.assertInGraveyard(player1, "Long River's Pull");
        harness.assertInGraveyard(player2, "Long River's Pull");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
