package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkycoachConductorAllAboard.class, SlumberingTrudge.class})
class SkycoachConductorAllAboardTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Skycoach Conductor")
    void entersPrepared() {
        Permanent conductor = castSkycoachConductor();

        assertThat(conductor.isPrepared()).isTrue();
        UUID copyId = conductor.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting All Aboard unprepares Skycoach Conductor and flickers a non-Pilot creature")
    void castingAllAboardFlickersNonPilotCreature() {
        Permanent conductor = castSkycoachConductor();
        Permanent target = addCreatureReady(player1, new SlumberingTrudge());
        UUID targetId = target.getId();
        UUID copyId = conductor.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, targetId);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Slumbering Trudge");
        assertThat(conductor.isPrepared()).isFalse();
        assertThat(conductor.getPreparedSpellCardId()).isNull();
        assertThat(returned.getId()).isNotEqualTo(targetId);
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("A Pilot creature is not a legal All Aboard target")
    void cannotTargetPilotCreature() {
        Permanent conductor = castSkycoachConductor();
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new SkycoachConductorAllAboard());
        UUID copyId = conductor.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId,
                pilot.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering prepared does not create an enters trigger")
    void preparationDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new SkycoachConductorAllAboard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Skycoach Conductor").isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All Aboard cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent conductor = castSkycoachConductor();
        Permanent opponentCreature = addCreatureReady(player2, new SlumberingTrudge());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1,
                conductor.getPreparedSpellCardId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conductor.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("All Aboard returns a borrowed creature to its owner")
    void returnsCreatureToItsOwner() {
        Permanent conductor = castSkycoachConductor();
        Permanent borrowed = addCreatureReady(player1, new SlumberingTrudge());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, conductor.getPreparedSpellCardId(), borrowed.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Slumbering Trudge")).isEmpty();
        assertThat(findPermanent(player2, "Slumbering Trudge").getId()).isNotEqualTo(borrowed.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Casting All Aboard immediately after entry does not prepare the Conductor again")
    void immediateCastDoesNotGrantAnotherPrepareSpell() {
        Permanent target = addCreatureReady(player1, new SlumberingTrudge());
        harness.setHand(player1, List.of(new SkycoachConductorAllAboard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent conductor = findPermanent(player1, "Skycoach Conductor");

        harness.castFromExile(player1, conductor.getPreparedSpellCardId(), target.getId());
        assertThat(conductor.isPrepared()).isFalse();
        resolveAllTriggers();

        assertThat(conductor.isPrepared()).isFalse();
        assertThat(conductor.getPreparedSpellCardId()).isNull();
        assertThat(findPermanent(player1, "Slumbering Trudge").getId()).isNotEqualTo(target.getId());
    }

    private Permanent castSkycoachConductor() {
        harness.setHand(player1, List.of(new SkycoachConductorAllAboard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Skycoach Conductor");
    }
}
