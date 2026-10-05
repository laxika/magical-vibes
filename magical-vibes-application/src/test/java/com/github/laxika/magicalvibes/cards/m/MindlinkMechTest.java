package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AoTheDawnSky;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarmoniousEmergence;
import com.github.laxika.magicalvibes.cards.w.WalkingSkyscraper;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindlinkMech.class, WalkingSkyscraper.class, AoTheDawnSky.class,
        Forest.class, HarmoniousEmergence.class})
class MindlinkMechTest extends BaseCardTest {

    @Test
    void becomesCopyOfTheCreatureThatCrewedIt() {
        Permanent mech = addReadyMech(player1);
        Permanent crewer = addCreatureReady(player1, new WalkingSkyscraper());

        crew(player1, mech, crewer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(crewer.getId());

        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mech)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mech)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, mech)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mech, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mech, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mech)).isFalse();
    }

    @Test
    void doesNotTargetLegendaryCreature() {
        Permanent mech = addReadyMech(player1);
        Permanent legendaryCreature = addCreatureReady(player1, new AoTheDawnSky());

        crew(player1, mech, legendaryCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, mech)).isTrue();
    }

    @Test
    void copiesAbilitiesButNotCrewersCountersOrTappedState() {
        Permanent mech = addReadyMech(player1);
        mech.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent crewer = addCreatureReady(player1, new WalkingSkyscraper());
        crewer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new WalkingSkyscraper());

        crew(player1, mech, crewer);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(crewer.getId());
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mech)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mech, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mech, Keyword.HEXPROOF)).isTrue();
        assertThat(crewer.isTapped()).isTrue();
        assertThat(mech.isTapped()).isFalse();

        mech.setTapped(true);
        assertThat(gqs.hasKeyword(gd, mech, Keyword.HEXPROOF)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, mech, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void firstCrewingWithOnlyLegendaryCreatureStillUsesTheTriggerForTheTurn() {
        Permanent mech = addReadyMech(player1);
        Permanent legendaryCreature = addCreatureReady(player1, new AoTheDawnSky());
        Permanent laterCrewer = addCreatureReady(player1, new WalkingSkyscraper());

        crew(player1, mech, legendaryCreature);
        crew(player1, mech, laterCrewer);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.hasKeyword(gd, mech, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mech)).isEqualTo(3);
    }

    @Test
    void copyingAnAnimatedLandWithoutCopiablePowerAndToughnessDies() {
        Permanent mech = addReadyMech(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HarmoniousEmergence()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        crew(player1, mech, forest);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mech);
        harness.assertInGraveyard(player1, "Mindlink Mech");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    private Permanent addReadyMech(Player player) {
        return addCreatureReady(player, new MindlinkMech());
    }

    private void crew(Player player, Permanent mech, Permanent crewer) {
        int mechIndex = gd.playerBattlefields.get(player.getId()).indexOf(mech);
        harness.activateAbility(player, mechIndex, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
