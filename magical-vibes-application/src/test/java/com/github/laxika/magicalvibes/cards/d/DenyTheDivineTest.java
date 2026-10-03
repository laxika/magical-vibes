package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenyTheDivine.class, AngelicChorus.class, GrizzlyBears.class, MightOfOaks.class,
        DestinySpinner.class, NyxbornCourser.class})
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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
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
        harness.castAndResolveInstant(player2, 0, chorus.getId());

        harness.assertNotInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
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

    @Test
    @DisplayName("Counters and exiles an enchantment creature spell")
    void countersEnchantmentCreatureSpell() {
        NyxbornCourser courser = new NyxbornCourser();
        harness.setHand(player1, List.of(courser));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new DenyTheDivine()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, courser.getId());

        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        harness.assertNotInGraveyard(player1, "Nyxborn Courser");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(courser);
        harness.assertInGraveyard(player2, "Deny the Divine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncounterable spell is a legal target but is not exiled")
    void doesNotExileUncounterableSpell() {
        harness.addToBattlefield(player1, new DestinySpinner());
        NyxbornCourser courser = new NyxbornCourser();
        harness.setHand(player1, List.of(courser));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new DenyTheDivine()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, courser.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(courser);
        harness.assertNotInGraveyard(player1, "Nyxborn Courser");
        harness.assertInGraveyard(player2, "Deny the Divine");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    @DisplayName("Can counter your own creature spell and exile it in its owner's zone")
    void countersOwnSpell() {
        NyxbornCourser courser = new NyxbornCourser();
        harness.setHand(player1, List.of(courser, new DenyTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, courser.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(courser);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(courser);
        harness.assertNotInGraveyard(player1, "Nyxborn Courser");
        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        harness.assertInGraveyard(player1, "Deny the Divine");
        assertThat(gd.stack).isEmpty();
    }
}
