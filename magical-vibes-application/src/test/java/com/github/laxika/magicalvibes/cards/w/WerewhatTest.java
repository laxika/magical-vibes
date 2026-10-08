package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Werewhat.class, GrizzlyBears.class, HillGiant.class,
        DelverOfSecrets.class, InsectileAberration.class, ElvishVisionary.class})
class WerewhatTest extends BaseCardTest {

    @Test
    void mayExileCreatureFromGraveyardAndUseItAsDynamicBackFace() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.WerewhatOnEnterChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent werewhat = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(werewhat.getOriginalCard().getBackFaceCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(werewhat.isTransformed()).isFalse();
    }

    @Test
    void exilingFromHandDrawsAndNightTransformsToTheChosenCreature() {
        gd.dayNight = DayNight.NIGHT;
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant drawn = new HillGiant();
        harness.setHand(player1, List.of(new Werewhat(), bears));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent werewhat = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(werewhat.isTransformed()).isTrue();
        assertThat(werewhat.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void linkedCreatureReturnsToItsOwnersGraveyardWhenWerewhatLeaves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent werewhat = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, werewhat));

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    void entersWithoutAChoiceWhenNoCreatureCardIsAvailable() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Werewhat");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void mayDeclineExilingWithoutDrawingOrRemovingTheCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant drawn = new HillGiant();
        harness.setHand(player1, List.of(new Werewhat(), bears));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();
    }

    @Test
    void exilingFromGraveyardDoesNotDraw() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant drawn = new HillGiant();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void returningAndRecastingWithoutExilingDoesNotRetainTheOldBackFace() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent werewhat = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, werewhat));
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        var returnedWerewhat = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Werewhat")).findFirst().orElseThrow();
        gd.dayNight = DayNight.NIGHT;
        harness.castFromHand(player1, returnedWerewhat, "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Werewhat");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();
    }

    @Test
    void exilingADoubleFacedCreatureOffersAChoiceOfFace() {
        DelverOfSecrets delver = new DelverOfSecrets();
        harness.setGraveyard(player1, List.of(delver));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(delver.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.WerewhatOnEnterChoice.class);
    }

    @Test
    void enteringAtNightTriggersTheChosenBackFacesEntersAbility() {
        gd.dayNight = DayNight.NIGHT;
        ElvishVisionary visionary = new ElvishVisionary();
        HillGiant drawn = new HillGiant();
        harness.setGraveyard(player1, List.of(visionary));
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new Werewhat(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(visionary.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Visionary");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
