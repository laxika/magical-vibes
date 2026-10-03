package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherSyphon.class})
class AetherSyphonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Aether Syphon draws a card and taps it")
    void activatingDrawsAndTaps() {
        Permanent syphon = addSyphon();
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof AetherSyphon);
        assertThat(syphon.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("At max speed, drawing with Aether Syphon mills two cards from each opponent")
    void maxSpeedMillsEachOpponent() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Aether Syphon does not mill below max speed")
    void belowMaxSpeedDoesNotMill() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private Permanent addSyphon() {
        return harness.addToBattlefieldAndReturn(player1, new AetherSyphon());
    }

    @Test
    void enteringStartsEnginesWithoutResettingExistingSpeed() {
        harness.enterBattlefieldAndReturn(player1, new AetherSyphon());
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.enterBattlefieldAndReturn(player1, new AetherSyphon());
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void eachCardDrawnFromAnotherSourceTriggersMilling() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new AetherSyphon(), new AetherSyphon(), new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon(),
                new AetherSyphon(), new AetherSyphon(), new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentDrawingDoesNotTriggerMilling() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsWhenOpponentLibraryHasOneCard() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void triggeredMillingStillResolvesAfterSpeedDrops() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).hasSize(1);
        gd.playerSpeeds.put(player1.getId(), 3);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void triggeredMillingStillResolvesAfterSyphonLeavesBattlefield() {
        Permanent syphon = addSyphon();
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(syphon);
        gd.playerGraveyards.get(player1.getId()).add(syphon.getCard());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void drawBelowMaxSpeedDoesNotCreateTriggerThatCanResolveAfterSpeedIncreases() {
        addSyphon();
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(new AetherSyphon()));
        harness.setLibrary(player2, List.of(new AetherSyphon(), new AetherSyphon()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).isEmpty();
        gd.playerSpeeds.put(player1.getId(), 4);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent syphon = addSyphon();
        syphon.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        Permanent syphon = addSyphon();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(syphon.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
