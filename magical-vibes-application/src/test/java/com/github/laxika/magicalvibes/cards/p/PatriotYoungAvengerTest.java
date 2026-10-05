package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatriotYoungAvenger.class, GrizzlyBears.class, BoneSaw.class, ViridianLongbow.class})
class PatriotYoungAvengerTest extends BaseCardTest {

    @Test
    void prowessBoostsPatriotWhenCastingNoncreatureSpell() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        harness.setHand(player1, List.of(new BoneSaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(3);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
    }

    @Test
    void equipmentBoostsOtherCreaturesButNotPatriot() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        equipment.setAttachedTo(patriot.getId());

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void prowessStacksAndExpiresAtEndOfTurn() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        harness.setHand(player1, List.of(new BoneSaw(), new BoneSaw()));

        harness.castArtifact(player1, 0);
        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(3);
        resolveAllTriggers();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
    }

    @Test
    void opponentNoncreatureSpellDoesNotTriggerProwess() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BoneSaw()));

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
    }

    @Test
    void opponentEquipmentEnablesBonusOnlyForPatriotsController() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new ViridianLongbow());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        equipment.setAttachedTo(patriot.getId());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);

        equipment.setAttachedTo(ally.getId());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
    }

    @Test
    void multipleEquipmentDoesNotMultiplyBonusAndLastDetachmentRemovesIt() {
        Permanent patriot = addCreatureReady(player1, new PatriotYoungAvenger());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        first.setAttachedTo(patriot.getId());
        second.setAttachedTo(patriot.getId());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        first.setAttachedTo(null);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        second.setAttachedTo(null);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
    }
}
