package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShalaiVoiceOfPlenty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void nonlegendaryCreatureDoesNotTriggerWhenThereIsNoMonarch() {
        harness.addToBattlefield(player1, new CrownOfGondor());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void opponentsLegendaryCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrownOfGondor());

        harness.enterBattlefieldAndReturn(player2, new ShalaiVoiceOfPlenty());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void existingMonarchPreventsTriggerFromBeingQueued() {
        harness.addToBattlefield(player1, new CrownOfGondor());
        gd.monarchPlayerId = player2.getId();

        harness.enterBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void doesNotTakeMonarchyIfSomeoneBecomesMonarchBeforeResolution() {
        harness.addToBattlefield(player1, new CrownOfGondor());
        harness.enterBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        assertThat(gd.stack).hasSize(1);

        gd.monarchPlayerId = player2.getId();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void equipCostsFourWhenOpponentIsMonarch() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfGondor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gd.monarchPlayerId = player2.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crown.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCostsFourWhenThereIsNoMonarch() {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfGondor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void bonusTracksYourCreatureCountAndExcludesOpponentsCreatures() {
        harness.addToBattlefield(player1, new CrownOfGondor());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        Permanent otherCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(otherCreature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
