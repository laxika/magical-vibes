package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAbyss.class, GrizzlyBears.class, Ornithopter.class})
class TheAbyssTest extends BaseCardTest {

    @Test
    void activePlayerChoosesAControlledNonartifactCreatureAndItCannotRegenerate() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent activeArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        activeCreature.setRegenerationShield(1);

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(activeCreature.getId())
                .doesNotContain(controllerCreature.getId(), activeArtifactCreature.getId());

        harness.handlePermanentChosen(player2, activeCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(controllerCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(activeArtifactCreature);
    }
}
