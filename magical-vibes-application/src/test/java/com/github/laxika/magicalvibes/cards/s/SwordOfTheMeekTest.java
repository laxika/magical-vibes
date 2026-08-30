package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DreadOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheMeek.class, DreadOfNight.class, GrizzlyBears.class, LlanowarElves.class, SilvercoatLion.class})
class SwordOfTheMeekTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardAndAttachesToEnteringOneOneCreature() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        Permanent elves = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());

        resolveMayAbility(true);

        Permanent returnedSword = findPermanent(player1, "Sword of the Meek");
        assertThat(returnedSword.getAttachedTo()).isEqualTo(elves.getId());
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Sword of the Meek");
    }

    @Test
    void usesEffectivePowerAndToughnessWhenCheckingTheEnteringCreature() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent lion = harness.enterBattlefieldAndReturn(player1, new SilvercoatLion());

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
        resolveMayAbility(true);

        Permanent returnedSword = findPermanent(player1, "Sword of the Meek");
        assertThat(returnedSword.getAttachedTo()).isEqualTo(lion.getId());
    }

    @Test
    void declineKeepsSwordInGraveyard() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        harness.enterBattlefieldAndReturn(player1, new LlanowarElves());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Sword of the Meek");
        harness.assertNotOnBattlefield(player1, "Sword of the Meek");
    }

    @Test
    void nonOneOneCreatureDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SwordOfTheMeek()));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsUnattachedIfEnteringCreatureLeavesBeforeResolution() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        Permanent elves = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, elves));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent returnedSword = findPermanent(player1, "Sword of the Meek");
        assertThat(returnedSword.getAttachedTo()).isNull();
        harness.assertNotInGraveyard(player1, "Sword of the Meek");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
