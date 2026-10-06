package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimianSimulacrum.class, ArgothianSprite.class, Disfigure.class})
class SimianSimulacrumTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts two +1/+1 counters on target creature you control")
    void etbPutsTwoCountersOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Unearth returns Simian Simulacrum with haste, triggers its ETB, and exiles it at end step")
    void unearthReturnsWithHasteTriggersEtbAndExilesAtEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setGraveyard(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent simian = findPermanent(player1, "Simian Simulacrum");
        assertThat(simian.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Simian Simulacrum");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Simian Simulacrum"));
    }

    @Test
    @DisplayName("Unearth can put its ETB counters on the returned Simian itself")
    void unearthCanTargetItself() {
        harness.setGraveyard(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent simian = findPermanent(player1, "Simian Simulacrum");
        harness.handlePermanentChosen(player1, simian.getId());
        harness.passBothPriorities();

        assertThat(simian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Simian Simulacrum");
    }

    @Test
    @DisplayName("Unearth exiles the creature instead of putting it into the graveyard")
    void unearthExilesInsteadOfDying() {
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setGraveyard(player1, List.of(new SimianSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sprite.getId());
        harness.passBothPriorities();
        Permanent simian = findPermanent(player1, "Simian Simulacrum");

        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, simian.getId());

        harness.assertNotOnBattlefield(player1, "Simian Simulacrum");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Simian Simulacrum"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Simian Simulacrum"));
    }
}
