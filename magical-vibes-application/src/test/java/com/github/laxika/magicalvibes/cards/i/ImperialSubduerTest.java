package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperialSubduer.class, ElvishWarrior.class, GrizzlyBears.class})
class ImperialSubduerTest extends BaseCardTest {

    @Test
    void samuraiAttackingAloneTapsTargetOpponentCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void warriorAttackingAloneTapsTargetOpponentCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void triggerDoesNotFireForNonSamuraiOrWarrior() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void triggerDoesNotFireWhenSamuraiOrWarriorDoesNotAttackAlone() {
        addCreatureReady(player1, new ImperialSubduer());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void triggerCannotTargetYourOwnCreature() {
        addCreatureReady(player1, new ImperialSubduer());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
