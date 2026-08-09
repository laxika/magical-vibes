package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlameJetTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new FlameJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsTheCardAndDrawsOne() {
        harness.setHand(player1, List.of(new FlameJet()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame Jet");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
