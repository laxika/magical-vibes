package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StraxSontaranNurse.class, GrizzlyBears.class, MindStone.class})
class StraxSontaranNurseTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactThenReflexivelyFightsAnotherCreature() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsAnyOf(ownCreature.getId(), opposingCreature.getId())
                .doesNotContain(strax.getId());

        Permanent selected = choice.validIds().contains(ownCreature.getId()) ? ownCreature : opposingCreature;
        harness.handlePermanentChosen(player1, selected.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(selected == ownCreature ? player1.getId() : player2.getId()))
                .doesNotContain(selected);
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
