package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HonoredHeirloom;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MilitiaRallier.class, TravelingMinister.class, HonoredHeirloom.class})
class MilitiaRallierTest extends BaseCardTest {

    @Test
    void attackTriggerUntapsTargetCreature() {
        addCreatureReady(player1, new MilitiaRallier());
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        target.tap();

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void attackTriggerOnlyTargetsCreatures() {
        addCreatureReady(player1, new MilitiaRallier());
        addCreatureReady(player1, new TravelingMinister());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HonoredHeirloom());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(artifact.getId());
    }

    @Test
    void cannotAttackAlone() {
        addCreatureReady(player1, new MilitiaRallier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canUntapItselfAndRemainAttacking() {
        Permanent rallier = addCreatureReady(player1, new MilitiaRallier());
        addCreatureReady(player1, new TravelingMinister());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(rallier.isTapped()).isTrue();
            harness.handlePermanentChosen(player1, rallier.getId());
            harness.passBothPriorities();
        });

        assertThat(rallier.isTapped()).isFalse();
        assertThat(rallier.isAttacking()).isTrue();
    }

    @Test
    void canTargetAnUntappedCreature() {
        addCreatureReady(player1, new MilitiaRallier());
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void doesNotUntapAnotherCreatureWhenTargetLeavesBattlefield() {
        Permanent rallier = addCreatureReady(player1, new MilitiaRallier());
        addCreatureReady(player1, new TravelingMinister());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        target.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            gd.playerBattlefields.get(player2.getId()).remove(target);
            gd.playerGraveyards.get(player2.getId()).add(target.getCard());
            harness.passBothPriorities();
        });

        assertThat(rallier.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBlockAlone() {
        addCreatureReady(player1, new TravelingMinister());
        addCreatureReady(player2, new MilitiaRallier());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
