package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        InduceParanoia.class,
        AirElemental.class,
        AvatarOfMight.class,
        Forest.class,
        GrizzlyBears.class,
        SpellbreakerBehemoth.class
})
class InduceParanoiaTest extends BaseCardTest {

    @Test
    @DisplayName("With black mana spent, counters the spell and mills its mana value")
    void blackManaMillsTargetSpellManaValue() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        prepareInduceWithBlackMana();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 5);
    }

    @Test
    @DisplayName("Without black mana, counters the spell without milling")
    void noBlackManaDoesNotMill() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("With black mana spent, mills even when the target cannot be countered")
    void blackManaMillsUncounterableSpell() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        AvatarOfMight avatar = new AvatarOfMight();
        harness.setHand(player1, List.of(avatar));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        prepareInduceWithBlackMana();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, avatar.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 8);
    }

    private void prepareInduceWithBlackMana() {
        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
    }
}
