package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CookingCampsite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidequestCatchAFish.class, CookingCampsite.class, GrizzlyBears.class, Millstone.class, Shock.class})
class SidequestCatchAFishTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an artifact or creature revealed from the top into hand, creates Food, and transforms")
    void matchingCreatureTransformsAndCreatesFood() {
        Permanent source = addSidequest(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(source.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Transforms for a noncreature artifact revealed from the top")
    void matchingArtifactTransforms() {
        Permanent source = addSidequest(player1);
        Card topCard = new Millstone();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(source.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Declining the reveal leaves the matching card on top and does not transform")
    void decliningRevealDoesNotTransform() {
        Permanent source = addSidequest(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(source.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("A non-artifact, noncreature top card does not offer a reveal")
    void nonmatchingTopCardDoesNotTransform() {
        Permanent source = addSidequest(player1);
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(source.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cooking Campsite sacrifices an artifact and puts counters on all controlled creatures")
    void campsiteSacrificesArtifactAndCountersCreatures() {
        Permanent campsite = addTransformedCampsite(player1);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(player1, campsite), 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherArtifact);
    }

    private Permanent addSidequest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SidequestCatchAFish());
    }

    private Permanent addTransformedCampsite(Player player) {
        SidequestCatchAFish front = new SidequestCatchAFish();
        Permanent campsite = new Permanent(front);
        campsite.setCard(front.getBackFaceCard());
        campsite.setTransformed(true);
        campsite.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(campsite);
        return campsite;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
