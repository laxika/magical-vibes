package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.c.CoralEel;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindingLight.class, ArmoredPegasus.class, CoralEel.class, Plains.class})
class BlindingLightTest extends BaseCardTest {

    private BlindingLight castBlindingLight() {
        BlindingLight blindingLight = new BlindingLight();
        harness.castFromHand(player1, blindingLight, "{2}{W}");
        harness.passBothPriorities();
        return blindingLight;
    }

    @Test
    @DisplayName("Taps nonwhite creatures on both sides")
    void tapsNonwhiteCreatures() {
        Permanent p1Creature = harness.addToBattlefieldAndReturn(player1, new CoralEel());
        Permanent p2Creature = harness.addToBattlefieldAndReturn(player2, new CoralEel());

        castBlindingLight();

        assertThat(p1Creature.isTapped()).isTrue();
        assertThat(p2Creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap white creatures")
    void doesNotTapWhiteCreatures() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new ArmoredPegasus());
        Permanent nonwhiteCreature = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        castBlindingLight();

        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(nonwhiteCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap noncreature permanents")
    void doesNotTapNoncreaturePermanents() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent nonwhiteCreature = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        castBlindingLight();

        assertThat(plains.isTapped()).isFalse();
        assertThat(nonwhiteCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Works with empty battlefield and resolves to graveyard")
    void worksWithEmptyBattlefield() {
        BlindingLight blindingLight = castBlindingLight();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(blindingLight);
    }
}
