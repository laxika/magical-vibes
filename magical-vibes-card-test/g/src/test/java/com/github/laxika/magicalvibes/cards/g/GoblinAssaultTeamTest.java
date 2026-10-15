package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinAssaultTeam.class, Shock.class, GrizzlyBears.class})
class GoblinAssaultTeamTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetCreatureYouControlWhenItDies() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinAssaultTeam()).getId();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(goblinId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentCreature() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinAssaultTeam()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(goblinId);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesOneFriendlyCreatureWithoutPuttingCountersOnOtherCreatures() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinAssaultTeam()).getId();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(goblinId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotRetargetWhenChosenCreatureDiesBeforeResolution() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new GoblinAssaultTeam()).getId();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(goblinId);
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killWithShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
