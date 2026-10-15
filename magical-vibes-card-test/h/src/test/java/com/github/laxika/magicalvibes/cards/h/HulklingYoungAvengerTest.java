package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FrostTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulklingYoungAvenger.class, GrizzlyBears.class, Divination.class, FrostTitan.class, Plains.class})
class HulklingYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell copies another creature with Hulkling's exceptions")
    void copiesAnotherCreatureWithExceptions() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDivination();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(hulkling.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(hulkling.getCard().getName()).isEqualTo("Hulkling, Young Avenger");
        assertThat(gqs.getEffectivePower(gd, hulkling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hulkling)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, hulkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Hulkling retains its copy trigger while copied")
    void retainsCopyTriggerWhileCopied() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDivination();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        castDivination();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        assertThat(hulkling.getCard().getName()).isEqualTo("Hulkling, Young Avenger");
    }

    @Test
    @DisplayName("Creature spells do not trigger Hulkling")
    void creatureSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new HulklingYoungAvenger());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void castDivination() {
        harness.castFromHand(player1, new Divination(), "{2}{U}");
    }

    @Test
    @DisplayName("Hulkling can decline to copy a creature")
    void canChooseNoTarget() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDivination();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hulkling)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hulkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Copying an opposing creature ends at cleanup and the copy ability returns")
    void opposingCreatureCopyEndsAtCleanup() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDivination();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();
        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hulkling);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, hulkling, Keyword.FLYING)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        castDivination();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();
        assertThat(gqs.hasEffectiveSubtype(gd, hulkling, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("A copied Frost Titan attack ability can target a land")
    void copiedAttackAbilityKeepsItsOwnTargetRestrictions() {
        Permanent hulkling = harness.addToBattlefieldAndReturn(player1, new HulklingYoungAvenger());
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new FrostTitan());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        hulkling.setSummoningSick(false);

        castDivination();
        harness.handlePermanentChosen(player1, titan.getId());
        resolveAllTriggers();
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(plains.getId());
        harness.handlePermanentChosen(player1, plains.getId());
        resolveAllTriggers();
        assertThat(plains.isTapped()).isTrue();
    }
}
