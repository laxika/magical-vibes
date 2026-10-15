package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemstoneCaverns.class, Forest.class})
class GemstoneCavernsTest extends BaseCardTest {

    @Test
    void onlyTheNonStartingPlayerGetsThePregameChoice() {
        harness.setHand(player1, List.of(new GemstoneCaverns(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        restartMulliganWithOpeningHands();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    void acceptingPregameChoicePlacesLuckCounterAndExilesAHandCard() {
        harness.setHand(player1, List.of(new GemstoneCaverns(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent gemstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gemstone Caverns"))
                .findFirst()
                .orElseThrow();
        assertThat(gemstone.getCounterCount(CounterType.LUCK)).isEqualTo(1);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    void acceptingPregameChoiceWorksWithNoOtherCardInHand() {
        harness.setHand(player1, List.of(new GemstoneCaverns()));
        harness.setHand(player2, List.of(new Forest()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Gemstone Caverns")
                        && permanent.getCounterCount(CounterType.LUCK) == 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void withoutLuckCounterItProducesColorlessMana() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void withLuckCounterItProducesTheChosenColor() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setSummoningSick(false);
        gemstone.setCounterCount(CounterType.LUCK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void startingPlayerCannotBeginWithGemstoneCaverns() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GemstoneCaverns(), new Forest()));

        restartMulliganWithOpeningHands();

        harness.assertInHand(player2, "Gemstone Caverns");
        harness.assertNotOnBattlefield(player2, "Gemstone Caverns");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningPregameChoiceLeavesBothCardsInHand() {
        harness.setHand(player1, List.of(new GemstoneCaverns(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Gemstone Caverns");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Gemstone Caverns");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void anotherGemstoneCavernsCanBeExiledAndItsQueuedChoiceCannotPutItOntoBattlefield() {
        GemstoneCaverns first = new GemstoneCaverns();
        GemstoneCaverns second = new GemstoneCaverns();
        harness.setHand(player1, List.of(first, second));
        harness.setHand(player2, List.of(new Forest()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(first.getId());
                    assertThat(permanent.getCounterCount(CounterType.LUCK)).isEqualTo(1);
                });
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).containsExactly(second.getId());
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void coloredManaAbilityIsUnavailableWithoutLuckCounter() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gemstone.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void colorlessManaAbilityIsUnavailableWithLuckCounter() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setCounterCount(CounterType.LUCK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gemstone.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void losingLastLuckCounterRestoresColorlessManaAbility() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setCounterCount(CounterType.LUCK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        gemstone.untap();
        gemstone.setCounterCount(CounterType.LUCK, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gemstone.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void multipleLuckCountersStillProduceOnlyOneManaOfAnyColor(ManaColor color) {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setCounterCount(CounterType.LUCK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
        assertThat(gemstone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void restartMulliganWithOpeningHands() {
        gd.status = GameStatus.MULLIGAN;
        gd.playerKeptHand.clear();
        gd.playerNeedsToBottom.clear();
        gd.playerMulliganDecisionIds.clear();
        gd.playerBottomDecisionIds.clear();
        gd.pendingMayAbilities.clear();
        gd.pendingGemstoneCavernsChoice = null;
        gd.interaction.clearAwaitingInput();
        gd.startingPlayerId = player2.getId();
        harness.skipMulligan();
    }
}
