package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoboRacetrack.class, Forest.class})
class ChocoboRacetrackTest extends BaseCardTest {

    @Test
    void landfallCreatesGreenBirdToken() {
        harness.addToBattlefield(player1, new ChocoboRacetrack());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent bird = findBird(player1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void birdLandfallBoostsItUntilEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new ChocoboRacetrack());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent bird = findBird(player1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bird.getEffectivePower()).isEqualTo(3);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void opponentLandfallDoesNotCreateBirdToken() {
        harness.addToBattlefield(player1, new ChocoboRacetrack());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD));
    }

    @Test
    void successiveLandEntriesBoostOnlyBirdsAlreadyOnTheBattlefield() {
        harness.addToBattlefield(player1, new ChocoboRacetrack());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        Permanent firstBird = findBird(player1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2)
                .extracting(Permanent::getEffectivePower).containsExactlyInAnyOrder(3, 2);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBird.getEffectivePower()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(3)
                .extracting(Permanent::getEffectivePower).containsExactlyInAnyOrder(4, 3, 2);
    }

    @Test
    void birdAbilityPersistsWithoutRacetrackAndIgnoresOpponentLands() {
        Permanent racetrack = harness.addToBattlefieldAndReturn(player1, new ChocoboRacetrack());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        Permanent bird = findBird(player1);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(racetrack);
        gd.playerGraveyards.get(player1.getId()).add(racetrack.getCard());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(bird.getEffectivePower()).isEqualTo(3);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);
    }

    private Permanent findBird(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .findFirst()
                .orElseThrow();
    }
}
