package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalSpawn.class, AvatarOfMight.class, GrizzlyBears.class, Forest.class})
class PrimevalSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("A Primeval Spawn that was not cast is exiled instead of entering")
    void uncastEntryIsExiled() {
        PrimevalSpawn card = new PrimevalSpawn();

        harness.enterBattlefieldAndReturn(player1, card);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("A normally cast Primeval Spawn enters the battlefield")
    void paidCastEnters() {
        PrimevalSpawn card = new PrimevalSpawn();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Primeval Spawn").getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("A Primeval Spawn cast for free is exiled instead of entering")
    void freeCastIsExiled() {
        PrimevalSpawn source = new PrimevalSpawn();
        PrimevalSpawn freeCast = new PrimevalSpawn();
        Permanent sourcePermanent = harness.addToBattlefieldAndReturn(player1, source);
        harness.setLibrary(player1, List.of(
                freeCast, new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sourcePermanent));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(freeCast.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Primeval Spawn")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(freeCast);
    }

    @Test
    @DisplayName("Its leave trigger casts a chosen subset with total mana value at most ten")
    void leaveTriggerUsesAggregateManaValueLimit() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PrimevalSpawn());
        AvatarOfMight avatar = new AvatarOfMight();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                avatar, firstBears, secondBears,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.maxTotalManaValue()).isEqualTo(10);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(avatar.getId(), firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(avatar.getId(), firstBears.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Avatar of Might")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(0);
    }
}
