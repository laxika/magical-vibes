package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentHallcreeper.class, Forest.class, GrizzlyBears.class})
class SilentHallcreeperTest extends BaseCardTest {

    private static final String COUNTERS = "Put two +1/+1 counters on Silent Hallcreeper";
    private static final String DRAW = "Draw a card";
    private static final String COPY = "Silent Hallcreeper becomes a copy of another target creature you control";

    @Test
    void cannotBeBlocked() {
        Permanent hallcreeper = addCreatureReady(player1, new SilentHallcreeper());
        addCreatureReady(player2, new GrizzlyBears());
        hallcreeper.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void putsTwoCountersOnItself() {
        Permanent hallcreeper = addCreatureReady(player1, new SilentHallcreeper());

        dealCombatDamageAndChoose(COUNTERS);

        assertThat(hallcreeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        addCreatureReady(player1, new SilentHallcreeper());

        dealCombatDamageAndChoose(DRAW);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void becomesACopyOfAnotherCreatureYouControl() {
        Permanent hallcreeper = addCreatureReady(player1, new SilentHallcreeper());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, COPY);
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        assertThat(hallcreeper.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(hallcreeper.getCard().getPower()).isEqualTo(2);
        assertThat(hallcreeper.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void copyModeIsUnavailableWithoutAnotherCreature() {
        addCreatureReady(player1, new SilentHallcreeper());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleListChoice(player1, COPY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void dealCombatDamageAndChoose(String mode) {
        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({SilentHallcreeper.class})
    void cannotChooseCountersAgainOnALaterCombat() {
        Permanent hallcreeper = addCreatureReady(player1, new SilentHallcreeper());
        dealCombatDamageAndChoose(COUNTERS);
        hallcreeper.untap();
        hallcreeper.setAttacking(false);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleListChoice(player1, COUNTERS))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(hallcreeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({SilentHallcreeper.class})
    void copyingAnotherHallcreeperGivesANewAbilityWithUnchosenModes() {
        Permanent hallcreeper = addCreatureReady(player1, new SilentHallcreeper());
        Permanent target = addCreatureReady(player1, new SilentHallcreeper());
        dealCombatDamageAndChoose(COUNTERS);
        hallcreeper.untap();
        hallcreeper.setAttacking(false);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, COPY);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(hallcreeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        hallcreeper.untap();
        hallcreeper.setAttacking(false);

        dealCombatDamageAndChoose(COUNTERS);

        assertThat(hallcreeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @CardUsed({SilentHallcreeper.class})
    void opposingCreatureDoesNotMakeCopyModeAvailable() {
        addCreatureReady(player1, new SilentHallcreeper());
        addCreatureReady(player2, new SilentHallcreeper());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleListChoice(player1, COPY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
