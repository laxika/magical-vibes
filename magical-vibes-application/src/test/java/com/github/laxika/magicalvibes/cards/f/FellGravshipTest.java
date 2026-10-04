package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GravpackMonoist;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellGravship.class, Forest.class, GravpackMonoist.class, LumenClassFrigate.class})
class FellGravshipTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards then returns a creature or Spacecraft card to hand")
    void entersMillsThenReturnsCreatureOrSpacecraft() {
        GravpackMonoist creature = new GravpackMonoist();
        LumenClassFrigate spacecraft = new LumenClassFrigate();
        Forest nonMatchingCard = new Forest();
        harness.setGraveyard(player1, List.of(creature, spacecraft, nonMatchingCard));
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, milled);

        castFellGravship();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(choice.validIndices().stream().map(graveyard::get))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), spacecraft.getId());
        int spacecraftIndex = graveyard.indexOf(spacecraft);

        harness.handleGraveyardCardChosen(player1, spacecraftIndex);

        harness.assertInHand(player1, "Lumen-Class Frigate");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(creature.getId(), nonMatchingCard.getId(),
                        milled.get(0).getId(), milled.get(1).getId(), milled.get(2).getId());
    }

    @Test
    @DisplayName("Eight charge counters animate Fell Gravship and grant flying and lifelink")
    void eightChargeCountersUnlockAbilities() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());

        assertThat(gqs.isCreature(gd, gravship)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.LIFELINK)).isFalse();

        gravship.setCounterCount(CounterType.CHARGE, 8);

        assertThat(gqs.isCreature(gd, gravship)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Station uses another creature's power and is unavailable without one")
    void stationUsesAnotherCreaturePower() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        Permanent bears = addCreatureReady(player1, new GravpackMonoist());

        harness.activateAbility(player1, battlefieldIndex(gravship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationRequiresAnotherUntappedCreature() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gravship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnCannotBeDeclinedWhenAnEligibleCardExists() {
        GravpackMonoist creature = new GravpackMonoist();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castFellGravship();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.assertInHand(player1, "Gravpack Monoist");
    }

    @Test
    void millingAndReturningOccurDuringTheSameTriggerResolution() {
        GravpackMonoist creature = new GravpackMonoist();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new FellGravship(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.assertInHand(player1, "Gravpack Monoist");
    }

    @Test
    void canReturnACreatureMilledFromAShortLibrary() {
        GravpackMonoist creature = new GravpackMonoist();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(creature, new Forest()));

        castFellGravship();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        harness.assertInHand(player1, "Gravpack Monoist");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void resolvesWithoutReturningWhenNoEligibleCardExists() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castFellGravship();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void stationCanTapASummoningSickCreatureWhileTheSpacecraftIsTapped() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        gravship.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GravpackMonoist());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(gravship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void stationCannotTapTheAnimatedSpacecraftItself() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        gravship.setCounterCount(CounterType.CHARGE, 8);
        gravship.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gravship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gravship.isTapped()).isFalse();
    }

    @Test
    void stationCannotTapAnAlreadyTappedOrOpposingCreature() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        Permanent creature = addCreatureReady(player1, new GravpackMonoist());
        creature.tap();
        addCreatureReady(player2, new GravpackMonoist());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gravship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        Permanent creature = addCreatureReady(player1, new GravpackMonoist());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gravship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void droppingBelowEightChargeCountersRemovesAnimationAndKeywords() {
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        gravship.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, gravship)).isTrue();

        gravship.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, gravship)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravship, Keyword.LIFELINK)).isFalse();
    }

    private void castFellGravship() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new FellGravship(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
