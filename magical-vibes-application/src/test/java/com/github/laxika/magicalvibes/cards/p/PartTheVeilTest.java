package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PartTheVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Returns only the caster's creatures to hand")
    void returnsOnlyControllersCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PartTheVeil()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.CREATURE));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return the caster's non-creature permanents")
    void doesNotReturnNonCreatures() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PartTheVeil()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield and goes to the graveyard")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new PartTheVeil()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Part the Veil");
    }
}
