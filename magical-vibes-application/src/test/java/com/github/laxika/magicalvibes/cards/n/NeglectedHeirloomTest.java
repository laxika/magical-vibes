package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CloisteredYouth;
import com.github.laxika.magicalvibes.cards.c.CivilizedScholar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeglectedHeirloom.class, CloisteredYouth.class, GrizzlyBears.class,
        CivilizedScholar.class, Moonmist.class})
class NeglectedHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Heirloom and gives the equipped creature +1/+1")
    void equipGivesPlusOnePlusOne() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Transforms into Ashmouth Blade when the equipped creature transforms")
    void transformsWithEquippedCreature() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());
        heirloom.setAttachedTo(youth.getId());

        transformYouth();
        harness.passBothPriorities(); // resolve the Heirloom's transform trigger

        assertThat(youth.isTransformed()).isTrue();
        assertThat(heirloom.isTransformed()).isTrue();
        assertThat(heirloom.getCard().getName()).isEqualTo("Ashmouth Blade");
        assertThat(heirloom.getAttachedTo()).isEqualTo(youth.getId());

        // Unholy Fiend is 3/3; Ashmouth Blade grants +3/+3 and first strike.
        assertThat(gqs.getEffectivePower(gd, youth)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, youth)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, youth, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not transform when an unequipped creature transforms")
    void doesNotTransformWhenNotAttached() {
        Permanent heirloom = addHeirloomReady(player1);
        harness.addToBattlefield(player1, new CloisteredYouth());

        transformYouth();

        assertThat(heirloom.isTransformed()).isFalse();
        assertThat(heirloom.getCard().getName()).isEqualTo("Neglected Heirloom");
    }

    @Test
    @DisplayName("Ashmouth Blade can be equipped for {3} and grants +3/+3 and first strike")
    void backFaceEquipsForThree() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());
        heirloom.setAttachedTo(youth.getId());

        transformYouth();
        harness.passBothPriorities(); // resolve the Heirloom's transform trigger

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Both Heirlooms attached to a transforming creature transform")
    void multipleAttachedHeirloomsTransform() {
        Permanent first = addHeirloomReady(player1);
        Permanent second = addHeirloomReady(player1);
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());
        first.setAttachedTo(youth.getId());
        second.setAttachedTo(youth.getId());

        transformYouth();
        resolveAllTriggers();

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, youth)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, youth)).isEqualTo(9);
    }

    @Test
    @DisplayName("Heirloom still transforms if the equipped creature leaves after triggering")
    void transformsAfterEquippedCreatureLeaves() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent youth = harness.addToBattlefieldAndReturn(player1, new CloisteredYouth());
        heirloom.setAttachedTo(youth.getId());

        transformYouth();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, youth));
        resolveAllTriggers();

        assertThat(heirloom.isTransformed()).isTrue();
        assertThat(heirloom.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An older transform trigger does not undo a newer trigger's transformation")
    void olderTransformTriggerDoesNotTransformHeirloomBack() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent scholar = addCreatureReady(player1, new CivilizedScholar());
        heirloom.setAttachedTo(scholar.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Moonmist()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(scholar.isTransformed()).isTrue();
        assertThat(heirloom.isTransformed()).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(scholar.isTransformed()).isFalse();
        resolveAllTriggers();

        assertThat(heirloom.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, scholar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scholar)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, scholar, Keyword.FIRST_STRIKE)).isTrue();
    }

    private void transformYouth() {
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability -> MayEffect prompts
        harness.handleMayAbilityChosen(player1, true);
    }

    private Permanent addHeirloomReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NeglectedHeirloom());
        perm.setSummoningSick(false);
        return perm;
    }
}
