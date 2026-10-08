package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TombTrawler.class, Forest.class})
class TombTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from your graveyard on the bottom of your library")
    void putsTargetCardOnBottomOfLibrary() {
        Permanent trawler = harness.addToBattlefieldAndReturn(player1, new TombTrawler());
        Card target = new Forest();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(trawler), 0,
                List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent trawler = harness.addToBattlefieldAndReturn(player1, new TombTrawler());
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(trawler), 0,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target more than one graveyard card")
    void rejectsMoreThanOneTarget() {
        Permanent trawler = harness.addToBattlefieldAndReturn(player1, new TombTrawler());
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(trawler), 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires one target even when your graveyard contains a card")
    void rejectsZeroTargets() {
        harness.addToBattlefield(player1, new TombTrawler());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a target when your graveyard is empty")
    void rejectsActivationWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new TombTrawler());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void activatesRepeatedlyWhileTappedAndSummoningSick() {
        Permanent trawler = harness.addToBattlefieldAndReturn(player1, new TombTrawler());
        trawler.tap();
        trawler.setSummoningSick(true);
        Card land = new Forest();
        Card creature = new TombTrawler();
        harness.setGraveyard(player1, List.of(land, creature));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(land.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), land.getId());
        assertThat(trawler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A second activation targeting the same card does nothing after it leaves the graveyard")
    void doesNotMoveTargetAgainAfterItLeavesGraveyard() {
        harness.addToBattlefield(player1, new TombTrawler());
        Card target = new Forest();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
    }

    @Test
    @DisplayName("Cannot activate with less than two mana")
    void rejectsInsufficientMana() {
        harness.addToBattlefield(player1, new TombTrawler());
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }
}
