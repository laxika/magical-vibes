package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrainGenerator.class, Forest.class, KorHaven.class})
class TerrainGeneratorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        Permanent generator = addGenerator();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a basic land from hand onto the battlefield tapped")
    void putsBasicLandTapped() {
        Permanent generator = addGenerator();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(generator.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
        Permanent forest = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only basic land cards are offered")
    void onlyOffersBasicLands() {
        addGenerator();
        harness.setHand(player1, List.of(new Forest(), new KorHaven()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice)
                harness.getGameData().interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Accepting with no basic land in hand does nothing")
    void acceptingWithNoBasicLandDoesNothing() {
        Permanent generator = addGenerator();
        KorHaven land = new KorHaven();
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may choice leaves the land in hand")
    void decliningMayChoiceLeavesHandUnchanged() {
        Permanent generator = addGenerator();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chooses exactly one basic land when multiple basic lands are in hand")
    void choosesOnlyOneBasicLand() {
        addGenerator();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(first, new KorHaven(), second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(second);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can put a basic land onto its controller's battlefield during an opponent's turn")
    void activatesDuringOpponentsTurn() {
        addGenerator();
        Forest land = new Forest();
        KorHaven opponentsLand = new KorHaven();
        harness.setHand(player1, List.of(land));
        harness.setHand(player2, List.of(opponentsLand));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(land);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addGenerator() {
        Permanent generator = harness.addToBattlefieldAndReturn(player1, new TerrainGenerator());
        generator.setSummoningSick(false);
        return generator;
    }
}
