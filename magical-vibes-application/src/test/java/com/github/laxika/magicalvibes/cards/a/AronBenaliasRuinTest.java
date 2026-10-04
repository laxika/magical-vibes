package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HauntedMire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AronBenaliasRuin.class, GrizzlyBears.class, AcademyWall.class, HauntedMire.class})
class AronBenaliasRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control after sacrificing another creature")
    void putsCountersOnEachCreature() {
        Permanent aron = addCreatureReady(player1, new AronBenaliasRuin());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(aron.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(aron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }

    @Test
    @DisplayName("Cannot sacrifice Aron itself")
    void requiresAnotherCreature() {
        addCreatureReady(player1, new AronBenaliasRuin());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyControlledCreaturesReceiveCountersAtResolution() {
        Permanent aron = addCreatureReady(player1, new AronBenaliasRuin());
        Permanent fodder = addCreatureReady(player1, new AcademyWall());
        Permanent opponentCreature = addCreatureReady(player2, new AcademyWall());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HauntedMire());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Academy Wall");
        assertThat(aron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new AcademyWall());
        harness.passBothPriorities();

        assertThat(aron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void summoningSickAronCannotActivate() {
        harness.addToBattlefield(player1, new AronBenaliasRuin());
        addCreatureReady(player1, new AcademyWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void tappedAronCannotActivate() {
        Permanent aron = addCreatureReady(player1, new AronBenaliasRuin());
        aron.tap();
        addCreatureReady(player1, new AcademyWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    void activationRequiresBothManaColors() {
        addCreatureReady(player1, new AronBenaliasRuin());
        addCreatureReady(player1, new AcademyWall());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new AronBenaliasRuin());
        addCreatureReady(player2, new AcademyWall());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new AronBenaliasRuin());
        Permanent first = addCreatureReady(player2, new AcademyWall());
        Permanent second = addCreatureReady(player2, new AcademyWall());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
