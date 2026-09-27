package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShalaiVoiceOfPlenty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfGondor.class, GrizzlyBears.class, ShalaiVoiceOfPlenty.class})
class CrownOfGondorTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOneForEachCreatureYouControl() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfGondor());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        gd.monarchPlayerId = player1.getId();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, equippedCreature.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(4);
    }

    @Test
    void becomesMonarchWhenLegendaryCreatureEntersAndThereIsNoMonarch() {
        harness.addToBattlefield(player1, new CrownOfGondor());

        harness.enterBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotBecomeMonarchForNonlegendaryCreatureOrWhenThereIsAlreadyAMonarch() {
        harness.addToBattlefield(player1, new CrownOfGondor());
        gd.monarchPlayerId = player2.getId();

        harness.enterBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }
}
