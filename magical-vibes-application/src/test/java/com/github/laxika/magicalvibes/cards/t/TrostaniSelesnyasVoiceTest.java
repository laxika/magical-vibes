package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrostaniSelesnyasVoice.class, GiantSpider.class, CallOfTheConclave.class,
        GiantGrowth.class, UltimatePrice.class})
class TrostaniSelesnyasVoiceTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering causes life gain equal to its toughness")
    void anotherCreatureEnteringGainsLifeEqualToToughness() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Trostani does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new TrostaniSelesnyasVoice()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Populate creates a copy of a creature token and triggers life gain")
    void populateCreatesTokenCopyAndGainsLife() {
        addCreatureReady(player1, new TrostaniSelesnyasVoice());
        harness.addToBattlefield(player1, creatureToken("Rhino Token", 4, 4));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rhino Token")).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void opposingCreatureEnteringDoesNotGainLife() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player2, List.of(new CallOfTheConclave()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player2, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeGainUsesToughnessAtResolution() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player1, List.of(new CallOfTheConclave(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent centaur = findPermanent(player1, "Centaur");
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 26);
    }

    @Test
    void lifeGainUsesLastKnownModifiedToughnessWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player1, List.of(new CallOfTheConclave(), new GiantGrowth(), new UltimatePrice()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent centaur = findPermanent(player1, "Centaur");
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Centaur")).isEmpty();
        harness.assertLife(player1, 26);
    }

    @Test
    void populateWithOnlyOpposingTokenCreatesNothingButPaysCosts() {
        Permanent trostani = addCreatureReady(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player2, List.of(new CallOfTheConclave()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player2, 0, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(trostani.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(trostani);
        assertThat(findPermanents(player2, "Centaur")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void populateCopiesBaseTokenWithoutTemporaryBoostOrTappedState() {
        harness.setHand(player1, List.of(new CallOfTheConclave(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent centaur = findPermanent(player1, "Centaur");
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        centaur.tap();
        Permanent trostani = addCreatureReady(player1, new TrostaniSelesnyasVoice());

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        List<Permanent> centaurs = findPermanents(player1, "Centaur");
        assertThat(centaurs).hasSize(2);
        Permanent copy = centaurs.stream().filter(p -> !p.getId().equals(centaur.getId())).findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(6);
        assertThat(trostani.isTapped()).isTrue();
        harness.assertLife(player1, 23);
    }

    @Test
    void populateChoosesAmongMultipleTokensDuringResolution() {
        harness.setHand(player1, List.of(new CallOfTheConclave(), new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent chosen = findPermanents(player1, "Centaur").getFirst();
        addCreatureReady(player1, new TrostaniSelesnyasVoice());

        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanents(player1, "Centaur")).hasSize(2);
        harness.handlePermanentChosen(player1, chosen.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Centaur")).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 23);
    }

    @Test
    void populateDoesNotCopyTokenRemovedInResponse() {
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent centaur = findPermanent(player1, "Centaur");
        addCreatureReady(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, centaur.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Centaur")).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void populateCannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private static Card creatureToken(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
