package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescendUponTheSinfulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all creatures and does not create an Angel without delirium")
    void exilesAllCreaturesWithoutDelirium() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("With delirium, creates a 4/4 flying Angel after exiling all creatures")
    void withDeliriumCreatesAngel() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        List<Permanent> angels = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Angel"))
                .toList();
        assertThat(angels).singleElement().satisfies(angel -> {
            assertThat(angel.getCard().getPower()).isEqualTo(4);
            assertThat(angel.getCard().getToughness()).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        });
    }

    private void cast() {
        harness.setHand(player1, List.of(new DescendUponTheSinful()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, 0);
    }
}
