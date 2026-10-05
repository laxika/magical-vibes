package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.e.EssenceOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalSpawn.class, AvatarOfMight.class, GrizzlyBears.class, Forest.class,
        Humility.class, SolRing.class, WearTear.class, EssenceOfTheWild.class})
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
        harness.castFromHand(player1, card, "{5}{W}{U}{B}{R}{G}");
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

    @Test
    @DisplayName("Humility allows an uncast Primeval Spawn to enter without its replacement ability")
    void humilitySuppressesEntryReplacement() {
        harness.addToBattlefield(player1, new Humility());
        PrimevalSpawn card = new PrimevalSpawn();

        harness.enterBattlefieldAndReturn(player1, card);

        assertThat(findPermanent(player1, "Primeval Spawn").getCard()).isSameAs(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("The aggregate limit permits Wear and Avatar of Might, whose spells total ten")
    void splitHalfUsesSpellManaValueInsteadOfCombinedCardManaValue() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PrimevalSpawn());
        harness.addToBattlefield(player2, new SolRing());
        AvatarOfMight avatar = new AvatarOfMight();
        WearTear splitCard = new WearTear();
        harness.setLibrary(player1, List.of(avatar, splitCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(avatar.getId(), splitCard.getId()));
        harness.handleListChoice(player1, "Wear — Destroy target artifact");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Sol Ring"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertInGraveyard(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(avatar, splitCard);
    }

    @Test
    @DisplayName("Declining all casts leaves the top ten cards exiled and the eleventh in the library")
    void decliningLeavesExactlyTopTenExiled() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PrimevalSpawn());
        GrizzlyBears bears = new GrizzlyBears();
        Forest eleventh = new Forest();
        List<Card> topTen = List.of(
                bears, new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        ArrayList<Card> library = new ArrayList<>(topTen);
        library.add(eleventh);
        harness.setLibrary(player1, library);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(topTen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eleventh);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exile from the battlefield triggers the ability even with a short library")
    void exileFromBattlefieldExilesEntireShortLibrary() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PrimevalSpawn());
        SolRing ring = new SolRing();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(ring, land));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, source));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ring.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source.getCard(), land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Being exiled instead of entering does not trigger the leave ability")
    void replacedEntryDoesNotExileLibrary() {
        PrimevalSpawn card = new PrimevalSpawn();
        SolRing ring = new SolRing();
        harness.setLibrary(player1, List.of(ring));

        harness.enterBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ring);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        harness.assertNotOnBattlefield(player1, "Sol Ring");
    }

    @Test
    @DisplayName("Wear // Tear cannot be fused when cast from exile by the leave ability")
    void fuseIsNotOfferedFromExile() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PrimevalSpawn());
        WearTear splitCard = new WearTear();
        harness.addToBattlefield(player2, new SolRing());
        harness.setLibrary(player1, List.of(splitCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.addToBattlefield(player2, new Humility());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(splitCard.getId()));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Wear — Destroy target artifact", "Tear — Destroy target enchantment");
    }

    @Test
    @DisplayName("An uncast Primeval Spawn can enter as a copy of Essence of the Wild")
    void copyingAnotherCreatureRemovesEntryReplacement() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        PrimevalSpawn card = new PrimevalSpawn();

        harness.enterBattlefieldAndReturn(player1, card);

        assertThat(findPermanents(player1, "Essence of the Wild")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }
}
