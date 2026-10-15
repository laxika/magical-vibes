package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleshlessGladiator.class})
class FleshlessGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard tapped and you lose 1 life with a corrupted opponent")
    void returnsTappedAndLosesLife() {
        FleshlessGladiator gladiator = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(gladiator.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(gladiator.getId()));
    }

    @Test
    @DisplayName("Requires an opponent to have three poison counters")
    void requiresOpponentPoisonThreshold() {
        FleshlessGladiator gladiator = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gladiator);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(gladiator.getId()));
    }

    @Test
    void returnsOnlyTheActivatedCopy() {
        FleshlessGladiator first = new FleshlessGladiator();
        FleshlessGladiator second = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(first, second));
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(second.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        harness.assertLife(player1, 19);
    }

    @Test
    void poisonThresholdIsNotRecheckedOnResolution() {
        harness.setGraveyard(player1, List.of(new FleshlessGladiator()));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateGraveyardAbility(player1, 0);
        harness.assertLife(player1, 20);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleshless Gladiator");
        harness.assertNotInGraveyard(player1, "Fleshless Gladiator");
        harness.assertLife(player1, 19);
    }

    @Test
    void repeatedActivationsLoseLifeEvenWhenTheCardAlreadyReturned() {
        harness.setGraveyard(player1, List.of(new FleshlessGladiator()));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Fleshless Gladiator");
    }

    @Test
    void canActivateDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new FleshlessGladiator()));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleshless Gladiator");
        harness.assertLife(player1, 19);
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        FleshlessGladiator gladiator = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(gladiator);
        harness.assertNotOnBattlefield(player1, "Fleshless Gladiator");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
