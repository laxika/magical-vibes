package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenyTheDivine.class, AngelicChorus.class, GrizzlyBears.class, MightOfOaks.class})
class DenyTheDivineTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and exiles it")
    void countersCreatureSpellAndExilesIt() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DenyTheDivine()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counters an enchantment spell and exiles it")
    void countersEnchantmentSpellAndExilesIt() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setHand(player1, List.of(chorus));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new DenyTheDivine()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chorus.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        org.assertj.core.api.Assertions.assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot target a noncreature nonenchantment spell")
    void cannotTargetNoncreatureNonenchantmentSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new DenyTheDivine()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
