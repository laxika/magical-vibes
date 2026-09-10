package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KabiraTakedown.class, KabiraPlateau.class, GrizzlyBears.class,
        ColossalDreadmaw.class, Millstone.class})
class KabiraTakedownTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToCreaturesControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTakedown(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new KabiraTakedown()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, target.getId());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Colossal Dreadmaw").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void damageModeCannotTargetAnArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new KabiraTakedown()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFaceEntersTappedAndProducesWhiteMana() {
        harness.setHand(player1, List.of(new KabiraTakedown()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void castTakedown(Permanent target) {
        harness.setHand(player1, List.of(new KabiraTakedown()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }
}
