package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Werewhat.class, GrizzlyBears.class, HillGiant.class})
class WerewhatTest extends BaseCardTest {

    @Test
    void mayExileCreatureFromGraveyardAndUseItAsDynamicBackFace() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Werewhat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new Werewhat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent werewhat = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, werewhat));

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }
}
