package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Reveille Squad")
@CardUsed(ReveilleSquad.class)
class ReveilleSquadTest extends BaseCardTest {

    @Test
    @DisplayName("May untap all creatures you control when creatures attack")
    void mayUntapAllCreaturesYouControl() {
        Permanent squad = addCreatureReady(player1, new ReveilleSquad());
        Permanent ally = addCreatureReady(player1, new ReveilleSquad());
        ally.tap();
        List<Permanent> attackers = addReadyAttackers(2);

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(squad.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(attackers).allSatisfy(attacker -> assertThat(attacker.isTapped()).isTrue());
    }

    @Test
    @DisplayName("May decline to untap all creatures you control")
    void mayDeclineToUntapAllCreaturesYouControl() {
        addCreatureReady(player1, new ReveilleSquad());
        Permanent ally = addCreatureReady(player1, new ReveilleSquad());
        ally.tap();
        addReadyAttackers(2);

        declareAttackers(player2, List.of(0, 1));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger while Reveille Squad is tapped")
    void doesNotTriggerWhileTapped() {
        Permanent squad = addCreatureReady(player1, new ReveilleSquad());
        squad.tap();
        addReadyAttackers(2);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does nothing if it becomes tapped before the trigger resolves")
    void doesNothingIfTappedBeforeResolution() {
        Permanent squad = addCreatureReady(player1, new ReveilleSquad());
        Permanent ally = addCreatureReady(player1, new ReveilleSquad());
        ally.tap();
        addReadyAttackers(2);

        declareAttackers(player2, List.of(0, 1));
        squad.tap();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ally.isTapped()).isTrue();
    }

    private List<Permanent> addReadyAttackers(int count) {
        List<Permanent> attackers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            attackers.add(addCreatureReady(player2, new ReveilleSquad()));
        }
        return attackers;
    }
}
