package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieldOfSouls;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulscourTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonartifact permanents and leaves artifacts on the battlefield")
    void destroysAllNonartifactPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new FieldOfSouls());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new IcyManipulator());

        harness.setHand(player1, List.of(new Soulscour()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player2, "Field of Souls");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Icy Manipulator");
        harness.assertInGraveyard(player1, "Soulscour");
    }
}
