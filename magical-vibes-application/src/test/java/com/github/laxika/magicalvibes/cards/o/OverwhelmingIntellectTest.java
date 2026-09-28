package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IdeasUnbound;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingIntellect.class, InnerChamberGuard.class, IdeasUnbound.class})
class OverwhelmingIntellectTest extends BaseCardTest {

    @Test
    void countersTargetCreatureSpell() {
        InnerChamberGuard creature = new InnerChamberGuard();
        harness.castFromHand(player1, creature, "{1}{W}");

        harness.setHand(player2, List.of(new OverwhelmingIntellect()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Inner-Chamber Guard");
        harness.assertNotOnBattlefield(player1, "Inner-Chamber Guard");
    }

    @Test
    void drawsCardsEqualToTargetCreatureSpellManaValue() {
        InnerChamberGuard creature = new InnerChamberGuard();
        harness.castFromHand(player1, creature, "{1}{W}");

        harness.setHand(player2, List.of(new OverwhelmingIntellect()));
        harness.setLibrary(player2, List.of(new InnerChamberGuard(), new InnerChamberGuard()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Overwhelming Intellect");
    }

    @Test
    void cannotTargetNonCreatureSpell() {
        IdeasUnbound spell = new IdeasUnbound();
        harness.castFromHand(player1, spell, "{U}{U}");

        OverwhelmingIntellect intellect = new OverwhelmingIntellect();
        harness.setHand(player2, List.of(intellect));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(intellect);
    }
}
