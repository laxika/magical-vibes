package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BotanicalBrawler;
import com.github.laxika.magicalvibes.cards.h.HideousFleshwheeler;
import com.github.laxika.magicalvibes.cards.r.RealmbreakersGrasp;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        NezumiFreewheeler.class,
        HideousFleshwheeler.class,
        Forest.class,
        BotanicalBrawler.class,
        RealmbreakersGrasp.class,
        VolcanicSpite.class
})
class NezumiFreewheelerTest extends BaseCardTest {

    @Test
    void entersAndMakesEachPlayerMillThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new NezumiFreewheeler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void transformsByPayingPhyrexianManaWithLife() {
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        assertThat(freewheeler.isTransformed()).isTrue();
        assertThat(freewheeler.getCard()).isInstanceOf(HideousFleshwheeler.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedTriggerReturnsTargetPermanentCardFromAnyGraveyard() {
        Card target = new BotanicalBrawler();
        Card nonPermanent = new VolcanicSpite();
        harness.setGraveyard(player1, List.of(nonPermanent));
        harness.setGraveyard(player2, List.of(target));
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(freewheeler.isTransformed()).isTrue();
    }

    @Test
    void transformsByPayingWhiteManaWithoutLosingLife() {
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        assertThat(freewheeler.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTransformDuringCombat() {
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, freewheeler), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(freewheeler.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsWhenLibrariesHaveFewerThanThree() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NezumiFreewheeler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void transformTriggerCanReturnALandButExcludesExpensivePermanents() {
        Card land = new Forest();
        Card expensive = new NezumiFreewheeler();
        harness.setGraveyard(player1, List.of(land, expensive));
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expensive);
    }

    @Test
    void transformTriggerDoesNotReturnTargetThatLeftTheGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void returnedAuraEntersAttachedToChosenLegalPermanent() {
        Card aura = new RealmbreakersGrasp();
        harness.setGraveyard(player2, List.of(aura));
        Permanent freewheeler = addReadyFreewheeler(player1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, freewheeler), null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, freewheeler.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(freewheeler.getId());
                });
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyFreewheeler(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NezumiFreewheeler());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
