package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZephyrScribe.class, Forest.class, Shock.class})
class ZephyrScribeTest extends BaseCardTest {

    @Test
    void activatingTheAbilityDrawsThenDiscards() {
        Permanent scribe = addReadyScribe();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(scribe.isTapped()).isTrue();
    }

    @Test
    void castingANoncreatureSpellUntapsZephyrScribe() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(scribe.isTapped()).isFalse();
    }

    @Test
    void castingACreatureSpellDoesNotUntapZephyrScribe() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player1, List.of(new ZephyrScribe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(scribe.isTapped()).isTrue();
    }

    @Test
    void canActivateWithAnEmptyHandAndDiscardTheDrawnCard() {
        Permanent scribe = addReadyScribe();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(scribe.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    void canDiscardTheCardJustDrawnInsteadOfTheOriginalHandCard() {
        addReadyScribe();
        Shock original = new Shock();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotUntapZephyrScribe() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(scribe.isTapped()).isTrue();
    }

    @Test
    void untapTriggerResolvesBeforeTheNoncreatureSpell() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        assertThat(scribe.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(scribe.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private Permanent addReadyScribe() {
        return addCreatureReady(player1, new ZephyrScribe());
    }

    private Permanent addTappedScribe() {
        Permanent scribe = addReadyScribe();
        scribe.tap();
        return scribe;
    }

}
