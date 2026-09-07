package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({BeetleLegacyCriminal.class, GrizzlyBears.class, Mountain.class})
class BeetleLegacyCriminalTest extends BaseCardTest {

    @Test
    @DisplayName("The graveyard ability puts a +1/+1 counter on target creature and grants flying")
    void putsCounterAndGrantsFlying() {
        Permanent target = addCreatureReady(player1);
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The graveyard ability exiles Beetle as an activation cost")
    void exilesBeetle() {
        Permanent target = addCreatureReady(player1);
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Beetle, Legacy Criminal");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Beetle, Legacy Criminal"));
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent target = addCreatureReady(player1);
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void targetsOpponentCreature() {
        Permanent target = addCreatureReady(player2);
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ability requires a creature target and sorcery speed")
    void requiresCreatureTargetAndSorcerySpeed() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new BeetleLegacyCriminal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent target = addCreatureReady(player1);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyAbility() {
        harness.setGraveyard(player1, List.of(new BeetleLegacyCriminal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
