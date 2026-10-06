package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.h.HarvesttideSentry;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.w.WordsOfWorship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardasSplendor.class, HarvesttideSentry.class, CandlegroveWitch.class,
        ReturnToNature.class, WordsOfWorship.class})
class SigardasSplendorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws at upkeep when life is at least the last noted total")
    void drawsWhenLifeIsAtLeastLastNotedTotal() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card topCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 21);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Updates the noted total even when the upkeep draw condition is false")
    void updatesNoteAfterMissedDraw() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card firstTopCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(firstTopCard));
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstTopCard);

        Card secondTopCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(secondTopCard));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(secondTopCard);
    }

    @Test
    @DisplayName("Gains 1 life whenever its controller casts a white spell")
    void gainsLifeWhenControllerCastsWhiteSpell() {
        castSigardasSplendor();
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new CandlegroveWitch(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when its controller casts a nonwhite spell")
    void doesNotGainLifeWhenControllerCastsNonwhiteSpell() {
        castSigardasSplendor();
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HarvesttideSentry(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void drawsAtEqualLifeTotal() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card topCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void usesLifeTotalAsAbilityResolvesRatherThanWhenUpkeepBegins() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card topCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 20);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void doesNotDrawIfLifeFallsBeforeResolution() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HarvesttideSentry()));

        advanceToUpkeep(player1);
        harness.setLife(player1, 19);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HarvesttideSentry()));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGainLifeFromOpponentsWhiteSpell() {
        castSigardasSplendor();
        int lifeBefore = gd.getLife(player1.getId());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CandlegroveWitch(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void upkeepStillDrawsAfterEnchantmentIsDestroyedInResponse() {
        castSigardasSplendor();
        harness.setHand(player1, List.of());
        Card topCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        advanceToUpkeep(player1);
        harness.castInstant(player2, 0, 1, harness.getPermanentId(player1, "Sigarda's Splendor"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sigarda's Splendor");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void notesLifeAfterDrawReplacementGainsLife() {
        castSigardasSplendor();
        harness.addToBattlefield(player1, new WordsOfWorship());
        harness.setHand(player1, List.of());
        Card topCard = new HarvesttideSentry();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.assertLife(player1, 25);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setLife(player1, 24);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private void castSigardasSplendor() {
        harness.castFromHand(player1, new SigardasSplendor(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
