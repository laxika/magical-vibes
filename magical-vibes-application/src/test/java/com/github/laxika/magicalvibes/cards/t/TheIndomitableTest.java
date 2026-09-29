package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheIndomitable.class, Forest.class, GrizzlyBears.class, RenegadeFreighter.class,
        TalasScout.class})
class TheIndomitableTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for each creature you control that deals combat damage to a player")
    void drawsForEachAllyCombatDamage() {
        harness.addToBattlefield(player1, new TheIndomitable());
        Permanent firstAttacker = addCreatureReady(new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Crew 3 animates the Vehicle and taps the creatures used to crew it")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new TheIndomitable());
        vehicle.setSummoningSick(false);
        Permanent firstCrew = addCreatureReady(new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast from the graveyard with three tapped Pirates and/or Vehicles")
    void canCastFromGraveyardWithThreeTappedPiratesOrVehicles() {
        Card indomitable = new TheIndomitable();
        harness.setGraveyard(player1, List.of(indomitable));
        addTappedQualifyingPermanent(new TalasScout());
        addTappedQualifyingPermanent(new TalasScout());
        addTappedQualifyingPermanent(new RenegadeFreighter());
        addManaForIndomitable();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TheIndomitable);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card == indomitable);
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard without three tapped Pirates and/or Vehicles")
    void cannotCastFromGraveyardWithoutThreeTappedPiratesOrVehicles() {
        harness.setGraveyard(player1, List.of(new TheIndomitable()));
        addTappedQualifyingPermanent(new TalasScout());
        addTappedQualifyingPermanent(new RenegadeFreighter());
        addManaForIndomitable();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    private Permanent addCreatureReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private Permanent addTappedQualifyingPermanent(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.tap();
        return permanent;
    }

    private void addManaForIndomitable() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
