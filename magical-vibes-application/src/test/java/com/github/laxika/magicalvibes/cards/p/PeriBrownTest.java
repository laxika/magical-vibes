package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.t.TheWarGames;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeriBrown.class, GrizzlyBears.class, HowlingMine.class, TheWarGames.class})
class PeriBrownTest extends BaseCardTest {

    @Test
    @DisplayName("The first historic spell each turn can use convoke")
    void firstHistoricSpellCanUseConvoke() {
        harness.addToBattlefield(player1, new PeriBrown());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof HowlingMine)
                .hasSize(1);
    }

    @Test
    @DisplayName("Only the first historic spell each turn gets convoke")
    void onlyFirstHistoricSpellGetsConvoke() {
        harness.addToBattlefield(player1, new PeriBrown());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourthCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine(), new HowlingMine()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(thirdCreature.getId(), fourthCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonhistoric spell does not use the historic spell allowance")
    void nonhistoricSpellDoesNotUseHistoricAllowance() {
        harness.addToBattlefield(player1, new PeriBrown());
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new HowlingMine()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(convokeCreature.getId(), secondConvokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A legendary creature spell can convoke its colored mana using Peri")
    void legendarySpellCanConvokeColoredMana() {
        Permanent peri = harness.addToBattlefieldAndReturn(player1, new PeriBrown());
        harness.setHand(player1, List.of(new PeriBrown()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(peri.getId()));

        assertThat(peri.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A historic spell cast before Peri enters still consumes the allowance")
    void historicSpellBeforePeriEntersConsumesAllowance() {
        harness.setHand(player1, List.of(new PeriBrown(), new HowlingMine()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Peri does not grant convoke to a nonhistoric spell")
    void nonhistoricSpellCannotConvoke() {
        harness.addToBattlefield(player1, new PeriBrown());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Peri does not grant convoke to your spell")
    void opponentsPeriDoesNotGrantConvoke() {
        harness.addToBattlefield(player2, new PeriBrown());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonlegendary Saga spell gets convoke as a historic spell")
    void sagaSpellCanConvoke() {
        Permanent peri = harness.addToBattlefieldAndReturn(player1, new PeriBrown());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheWarGames()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(peri.getId(), firstCreature.getId(), secondCreature.getId()));

        assertThat(peri.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
