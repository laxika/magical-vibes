package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceOut.class, DarksteelRelic.class, GrizzlyBears.class})
class IceOutTest extends BaseCardTest {

    @Test
    void countersSpellAtFullCost() {
        GrizzlyBears bears = castCreatureSpell();
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bargainCountersSpellForTwoManaAndSacrificesArtifact() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstantWithSacrifice(player2, 0, bears.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Darksteel Relic");
    }

    @Test
    void cannotBargainBySacrificingCreature() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player2, 0, bears.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    private GrizzlyBears castCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return bears;
    }
}
