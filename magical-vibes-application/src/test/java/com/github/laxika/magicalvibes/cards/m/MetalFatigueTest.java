package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetalFatigue.class, DarksteelIngot.class, DarksteelGargoyle.class, AuriokGlaivemaster.class})
class MetalFatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps every artifact on every battlefield")
    void tapsAllArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());

        castAndResolveMetalFatigue();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opposingArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap non-artifact permanents")
    void doesNotTapNonArtifacts() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AuriokGlaivemaster());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());

        castAndResolveMetalFatigue();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    private void castAndResolveMetalFatigue() {
        harness.setHand(player1, List.of(new MetalFatigue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
    }
}
