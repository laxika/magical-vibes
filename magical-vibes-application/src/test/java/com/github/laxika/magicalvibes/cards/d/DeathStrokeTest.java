package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTappedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a tapped land")
    void cannotTargetTappedLand() {
        Permanent plains = new Permanent(new Plains());
        plains.tap();
        gd.playerBattlefields.get(player2.getId()).add(plains);

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
