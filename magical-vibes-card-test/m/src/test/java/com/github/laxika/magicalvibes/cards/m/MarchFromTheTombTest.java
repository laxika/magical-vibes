package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
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
        GiantMantis.class})
class MarchFromTheTombTest extends BaseCardTest {

    @Test
    void targetsOnlyAllyCreaturesWithinTotalManaValueEight() {
        Card envoy = new ExpeditionEnvoy();
        Card stalwart = new TajuruStalwart();
        Card nonAlly = new GiantMantis();
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
        harness.assertInGraveyard(player1, "Giant Mantis");
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

    @Test
    void canChooseZeroTargetsWithAlliesAvailable() {
        Card envoy = new ExpeditionEnvoy();
        harness.setGraveyard(player1, List.of(envoy));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Expedition Envoy");
        harness.assertNotOnBattlefield(player1, "Expedition Envoy");
        harness.assertInGraveyard(player1, "March from the Tomb");
    }

    @Test
    void canCastWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "March from the Tomb");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsTargetsWithTotalManaValueExactlyEightSimultaneously() {
        Card stalwart = new TajuruStalwart();
        Card warcaller = new TajuruWarcaller();
        harness.setGraveyard(player1, List.of(stalwart, warcaller));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(stalwart.getId(), warcaller.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tajuru Stalwart");
        harness.assertOnBattlefield(player1, "Tajuru Warcaller");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        var returnedStalwart = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(stalwart.getId()))
                .findFirst().orElseThrow();
        assertThat(returnedStalwart.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, returnedStalwart)).isEqualTo(4);
    }

    @Test
    void cannotChooseAnAllyInOpponentsGraveyard() {
        Card ownEnvoy = new ExpeditionEnvoy();
        Card opponentsEnvoy = new ExpeditionEnvoy();
        harness.setGraveyard(player1, List.of(ownEnvoy));
        harness.setGraveyard(player2, List.of(opponentsEnvoy));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(opponentsEnvoy.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownEnvoy.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Envoy");
        harness.assertInGraveyard(player2, "Expedition Envoy");
        harness.assertNotOnBattlefield(player2, "Expedition Envoy");
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherLeavesTheGraveyard() {
        Card envoy = new ExpeditionEnvoy();
        Card stalwart = new TajuruStalwart();
        harness.setGraveyard(player1, List.of(envoy, stalwart));
        harness.setHand(player1, List.of(new MarchFromTheTomb()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(envoy.getId(), stalwart.getId()));
        harness.setGraveyard(player1, List.of(stalwart));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Expedition Envoy");
        harness.assertOnBattlefield(player1, "Tajuru Stalwart");
        harness.assertInGraveyard(player1, "March from the Tomb");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
