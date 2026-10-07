package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndyingMalice.class, HerosDownfall.class, TravelingMinister.class})
class UndyingMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the creature tapped under its owner's control with a +1/+1 counter")
    void returnsTappedWithCounterUnderOwnersControl() {
        Permanent target = addCreature(player2);
        Card targetCard = target.getCard();

        castUndyingMalice(player1, target.getId());
        castRemoval(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("The granted death trigger expires at end of turn")
    void deathTriggerExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        castUndyingMalice(player1, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castRemoval(player2, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its controller")
    void stolenCreatureReturnsToOwner() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();
        gd.stolenCreatures.put(target.getId(), player2.getId());

        castUndyingMalice(player1, target.getId());
        castRemoval(player2, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        harness.assertNotInGraveyard(player2, "Traveling Minister");
    }

    @Test
    @DisplayName("The returned creature does not retain the granted ability")
    void returnedCreatureDoesNotReturnAfterSecondDeath() {
        Permanent target = addCreature(player1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castUndyingMalice(player1, target.getId());
        castRemoval(player2, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Traveling Minister")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        UUID returnedId = harness.getPermanentId(player1, "Traveling Minister");
        castRemoval(player2, returnedId);

        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An older death trigger cannot return the creature after a second death")
    void olderTriggerCannotReturnNewGraveyardObject() {
        Permanent target = addCreature(player1);
        castUndyingMalice(player1, target.getId());
        castUndyingMalice(player1, target.getId());
        castRemoval(player2, target.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        UUID returnedId = harness.getPermanentId(player1, "Traveling Minister");
        assertThat(gd.stack).hasSize(1);

        castRemoval(player2, returnedId);
        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
    }

    @Test
    @DisplayName("The granted ability remains active during the end step")
    void creatureCanReturnDuringEndStep() {
        Permanent target = addCreature(player1);
        castUndyingMalice(player1, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Traveling Minister");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature killed in response does not gain the return ability")
    void targetDiesBeforeSpellResolves() {
        Permanent target = addCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UndyingMalice(), new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TravelingMinister());
    }

    private void castUndyingMalice(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new UndyingMalice()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private void castRemoval(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new HerosDownfall()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
