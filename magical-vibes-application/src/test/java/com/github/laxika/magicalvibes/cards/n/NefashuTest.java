package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nefashu.class, GrizzlyBears.class, FountainOfYouth.class})
class NefashuTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger gives up to five target creatures -1/-1")
    void attackTriggerShrinksUpToFiveCreatures() {
        addCreatureReady(player1, new Nefashu());
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            targets.add(addCreatureReady(i % 2 == 0 ? player1 : player2, new GrizzlyBears()));
        }

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        for (int i = 0; i < 5; i++) {
            harness.handlePermanentChosen(player1, targets.get(i).getId());
        }
        resolveAllTriggers();

        assertThat(targets.subList(0, 5)).allSatisfy(target -> {
            assertThat(target.getPowerModifier()).isEqualTo(-1);
            assertThat(target.getToughnessModifier()).isEqualTo(-1);
        });
        assertThat(targets.get(5).getPowerModifier()).isZero();
        assertThat(targets.get(5).getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack trigger can choose zero creatures")
    void attackTriggerCanChooseZeroCreatures() {
        addCreatureReady(player1, new Nefashu());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack trigger only allows creature targets")
    void attackTriggerOnlyAllowsCreatureTargets() {
        addCreatureReady(player1, new Nefashu());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(artifact.getId());
    }
}
