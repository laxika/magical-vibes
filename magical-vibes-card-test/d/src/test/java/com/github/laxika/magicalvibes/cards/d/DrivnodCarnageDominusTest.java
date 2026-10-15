package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Fecundity;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfaneMemento;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrivnodCarnageDominus.class, Fecundity.class, GrizzlyBears.class, Forest.class, CruelEdict.class})
class DrivnodCarnageDominusTest extends BaseCardTest {

    @Test
    void doublesCreatureDeathTriggersOfOtherPermanents() {
        harness.addToBattlefield(player1, new DrivnodCarnageDominus());
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void exilesThreeCreatureCardsAndPutsAnIndestructibleCounterOnIt() {
        Permanent drivnod = harness.addToBattlefieldAndReturn(player1, new DrivnodCarnageDominus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(drivnod.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void canPayBothPhyrexianSymbolsWithBlackMana() {
        Permanent drivnod = harness.addToBattlefieldAndReturn(player1, new DrivnodCarnageDominus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(drivnod.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(drivnod.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void canPayOnePhyrexianSymbolWithManaAndOneWithLife() {
        Permanent drivnod = harness.addToBattlefieldAndReturn(player1, new DrivnodCarnageDominus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(drivnod.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void cannotUseNoncreatureCardsOrOpponentsGraveyardToPayTheCost() {
        Permanent drivnod = harness.addToBattlefieldAndReturn(player1, new DrivnodCarnageDominus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(drivnod.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleDeathTriggersOfOpponentsPermanents() {
        harness.addToBattlefield(player1, new DrivnodCarnageDominus());
        harness.addToBattlefield(player2, new Fecundity());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @CardUsed({ProfaneMemento.class})
    void doublesFromAnywhereGraveyardTriggersWhenACreatureDies() {
        harness.addToBattlefield(player1, new DrivnodCarnageDominus());
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
