package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TajuruStalwart;
import com.github.laxika.magicalvibes.cards.t.TajuruWarcaller;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchFromTheTomb.class, ExpeditionEnvoy.class, TajuruStalwart.class, TajuruWarcaller.class,
        HillGiant.class})
class MarchFromTheTombTest extends BaseCardTest {

    @Test
    void targetsOnlyAllyCreaturesWithinTotalManaValueEight() {
        Card envoy = new ExpeditionEnvoy();
        Card stalwart = new TajuruStalwart();
        Card nonAlly = new HillGiant();
        harness.setGraveyard(player1, List.of(envoy, stalwart, nonAlly));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.maxTotalManaValue()).isEqualTo(8);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(envoy.getId(), stalwart.getId());

        harness.handleMultipleCardsChosen(player1, List.of(envoy.getId(), stalwart.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Envoy");
        harness.assertOnBattlefield(player1, "Tajuru Stalwart");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void rejectsTargetsOverTotalManaValueEight() {
        Card warcaller = new TajuruWarcaller();
        Card stalwart = new TajuruStalwart();
        Card envoy = new ExpeditionEnvoy();
        harness.setGraveyard(player1, List.of(warcaller, stalwart, envoy));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(warcaller.getId(), stalwart.getId(), envoy.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
