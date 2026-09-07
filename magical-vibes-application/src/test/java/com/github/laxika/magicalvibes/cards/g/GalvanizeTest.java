package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Galvanize.class, WorldspineWurm.class, GrizzlyBears.class})
class GalvanizeTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutTwoCardsDrawn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageAfterDrawingTwoCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        draw(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    private void castGalvanize(Permanent target) {
        harness.setHand(player1, List.of(new Galvanize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }
}
