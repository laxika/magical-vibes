package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DreadOfNight;
import com.github.laxika.magicalvibes.cards.k.KnightOfSursi;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheMeek.class, DreadOfNight.class, KnightOfSursi.class, NessianCourser.class,
        SamiteCenserBearer.class})
class SwordOfTheMeekTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardAndAttachesToEnteringOneOneCreature() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SamiteCenserBearer());

        resolveMayAbility(true);

        Permanent returnedSword = findPermanent(player1, "Sword of the Meek");
        assertThat(returnedSword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Sword of the Meek");
    }

    @Test
    void usesEffectivePowerAndToughnessWhenCheckingTheEnteringCreature() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new KnightOfSursi());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        resolveMayAbility(true);

        Permanent returnedSword = findPermanent(player1, "Sword of the Meek");
        assertThat(returnedSword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void declineKeepsSwordInGraveyard() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        harness.enterBattlefieldAndReturn(player1, new SamiteCenserBearer());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Sword of the Meek");
        harness.assertNotOnBattlefield(player1, "Sword of the Meek");
    }

    @Test
    void nonOneOneCreatureDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SwordOfTheMeek()));
        harness.enterBattlefieldAndReturn(player1, new NessianCourser());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureEnteringUnderOpponentsControlDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new SwordOfTheMeek()));
        harness.enterBattlefieldAndReturn(player2, new SamiteCenserBearer());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sword of the Meek");
    }

    @Test
    void equipAttachesToAnyCreatureYouControl() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheMeek());
        Permanent creature = addCreatureReady(player1, new NessianCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void returnsUnattachedIfEnteringCreatureLeavesBeforeResolution() {
        SwordOfTheMeek sword = new SwordOfTheMeek();
        harness.setGraveyard(player1, List.of(sword));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SamiteCenserBearer());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
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
