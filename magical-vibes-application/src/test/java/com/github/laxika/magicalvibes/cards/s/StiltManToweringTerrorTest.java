package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StiltManToweringTerror.class, SolRing.class, Forest.class, GrizzlyBears.class})
class StiltManToweringTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("A Villain's combat damage steals a noncreature, nonland permanent and prevents sacrificing it")
    void stealsAndProtectsTargetPermanent() {
        Permanent stiltMan = addCreatureReady(player1, new StiltManToweringTerror());
        stiltMan.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(artifact.getId())
                .doesNotContain(land.getId(), creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gqs.cantBeSacrificed(gd, artifact)).isTrue();
    }
}
