package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldStickfingers.class, GrizzlyBears.class, Plains.class, Shock.class})
class OldStickfingersTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger mills creature cards until X are found and bottoms the rest")
    void castTriggerMillsCreaturesAndBottomsNoncreatures() {
        Card firstNoncreature = new Shock();
        Card firstCreature = new GrizzlyBears();
        Card secondNoncreature = new Plains();
        Card secondCreature = new GrizzlyBears();
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(
                firstNoncreature, firstCreature, secondNoncreature, secondCreature, untouched));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Old Stickfingers");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCreature, secondCreature);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst()).isSameAs(untouched);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);

        Permanent oldStickfingers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Old Stickfingers"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, oldStickfingers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldStickfingers)).isEqualTo(2);
    }

    @Test
    @DisplayName("If the library ends first, every revealed creature still goes to the graveyard")
    void libraryCanEndBeforeXCreaturesAreFound() {
        Card noncreature = new Shock();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(noncreature, creature));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("X=0 reveals nothing and the creature's power equals existing creature cards in the graveyard")
    void xZeroRevealsNothing() {
        Card libraryCard = new Shock();
        Card graveyardCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        Permanent oldStickfingers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Old Stickfingers"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, oldStickfingers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oldStickfingers)).isEqualTo(1);
    }
}
