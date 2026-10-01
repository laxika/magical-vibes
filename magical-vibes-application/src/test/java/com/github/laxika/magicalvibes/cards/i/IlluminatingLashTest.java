package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IlluminatingLash.class, Forest.class, GrizzlyBears.class, Shock.class})
class IlluminatingLashTest extends BaseCardTest {

    @Test
    void dealsDamageAndDrawsForTheNextNoncreatureSpell() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new IlluminatingLash()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void cannotTargetALand() {
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new IlluminatingLash()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
