package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacheGrab.class, SquirrelMob.class, Forest.class, LightningBolt.class})
class CacheGrabTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards and may return a permanent card to hand")
    void returnsSelectedPermanent() {
        Card returned = new SquirrelMob();
        setLibrary(returned, new Forest(), new Forest(), new Forest());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when a Squirrel is controlled")
    void createsFoodForControlledSquirrel() {
        harness.addToBattlefield(player1, new SquirrelMob());
        setLibrary(new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt());

        castCacheGrab();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates Food when returning another permanent while controlling a Squirrel")
    void createsFoodForControlledSquirrelAfterReturningOtherPermanent() {
        harness.addToBattlefield(player1, new SquirrelMob());
        setLibrary(new Forest(), new LightningBolt(), new LightningBolt(), new LightningBolt());

        castCacheGrab();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Does not create Food without a Squirrel")
    void doesNotCreateFoodWithoutSquirrel() {
        setLibrary(new SquirrelMob(), new LightningBolt(), new LightningBolt(), new LightningBolt());

        castCacheGrab();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Food");
    }

    private void castCacheGrab() {
        harness.setHand(player1, List.of(new CacheGrab()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, new ArrayList<>(List.of(cards)));
    }
}
