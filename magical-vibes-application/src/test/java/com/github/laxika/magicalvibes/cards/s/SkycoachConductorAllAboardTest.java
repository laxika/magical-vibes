package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        UUID targetId = target.getId();
        UUID copyId = conductor.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, targetId);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
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
        GrizzlyBears pilot = new GrizzlyBears();
        pilot.setSubtypes(List.of(CardSubtype.BEAR, CardSubtype.PILOT));
        harness.addToBattlefield(player1, pilot);
        UUID copyId = conductor.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId,
                gd.playerBattlefields.get(player1.getId()).getLast().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castSkycoachConductor() {
        harness.setHand(player1, List.of(new SkycoachConductorAllAboard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Skycoach Conductor");
    }
}
