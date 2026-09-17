package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimpseTheImpossible.class, Forest.class, Mountain.class})
class GlimpseTheImpossibleTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top three cards and lets them be played this turn")
    void exilesTopThreeCardsForTheTurn() {
        Card first = new Forest();
        Card second = new Mountain();
        Card third = new Forest();

        castGlimpse(first, second, third);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
    }

    @Test
    @DisplayName("Puts unplayed cards into the graveyard and creates one Spawn per card")
    void unplayedCardsBecomeSpawnTokens() {
        Card played = new Mountain();
        Card unplayed = new Forest();
        Card secondUnplayed = new Mountain();

        castGlimpse(played, unplayed, secondUnplayed);
        gs.playCardFromExile(gd, player1, played.getId(), null, null);
        resolveNextEndStep();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(unplayed.getId(), secondUnplayed.getId());
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("Eldrazi Spawn tokens sacrifice for colorless mana")
    void spawnTokensSacrificeForColorlessMana() {
        castGlimpse(new Forest(), new Forest(), new Forest(), new Forest());
        resolveNextEndStep();

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when every exiled card was played")
    void doesNotTriggerWhenNoExiledCardsRemain() {
        Card card = new Forest();
        castGlimpse(card);
        gs.playCardFromExile(gd, player1, card.getId(), null, null);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(StepTriggerService.class)
                .handleEndStepTriggers(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    private void castGlimpse(Card... libraryCards) {
        harness.setLibrary(player1, List.of(libraryCards));
        harness.setHand(player1, List.of(new GlimpseTheImpossible()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void resolveNextEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(StepTriggerService.class)
                .handleEndStepTriggers(gd));
        resolveAllTriggers();
    }
}
