package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.cards.s.SkyDiamond;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindingLight.class, EkunduGriffin.class, MtendaLion.class, SkyDiamond.class})
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
        Permanent p1Lion = harness.addToBattlefieldAndReturn(player1, new MtendaLion());
        Permanent p2Lion = harness.addToBattlefieldAndReturn(player2, new MtendaLion());

        castBlindingLight();

        assertThat(p1Lion.isTapped()).isTrue();
        assertThat(p2Lion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap white creatures")
    void doesNotTapWhiteCreatures() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new EkunduGriffin());
        Permanent nonwhiteCreature = harness.addToBattlefieldAndReturn(player1, new MtendaLion());

        castBlindingLight();

        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(nonwhiteCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap noncreature permanents")
    void doesNotTapNoncreaturePermanents() {
        Permanent skyDiamond = harness.addToBattlefieldAndReturn(player1, new SkyDiamond());
        Permanent nonwhiteCreature = harness.addToBattlefieldAndReturn(player1, new MtendaLion());

        castBlindingLight();

        assertThat(skyDiamond.isTapped()).isFalse();
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
