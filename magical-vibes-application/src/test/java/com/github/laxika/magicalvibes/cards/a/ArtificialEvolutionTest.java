package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtificialEvolution.class, GoblinKing.class, LlanowarElves.class})
class ArtificialEvolutionTest extends BaseCardTest {

    @Test
    void changesCreatureTypeTextOnPermanent() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent elves = findPermanent(player1, "Llanowar Elves");
        UUID targetId = harness.getPermanentId(player1, "Goblin King");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(2);
    }

    @Test
    void cannotChooseWallAsReplacementCreatureType() {
        harness.addToBattlefield(player1, new GoblinKing());
        UUID targetId = harness.getPermanentId(player1, "Goblin King");

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "WALL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void creatureTypeChangeOnSpellCarriesToPermanent() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new GoblinKing()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player1, "ELF");
        harness.passBothPriorities();

        Permanent elves = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(2);
    }
}
