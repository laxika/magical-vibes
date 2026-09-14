package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AoTheDawnSky;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindlinkMech.class, GrizzlyBears.class, AoTheDawnSky.class})
class MindlinkMechTest extends BaseCardTest {

    @Test
    void becomesCopyOfTheCreatureThatCrewedIt() {
        Permanent mech = addReadyMech(player1);
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());

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

    private Permanent addReadyMech(Player player) {
        Permanent mech = harness.addToBattlefieldAndReturn(player, new MindlinkMech());
        mech.setSummoningSick(false);
        return mech;
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
