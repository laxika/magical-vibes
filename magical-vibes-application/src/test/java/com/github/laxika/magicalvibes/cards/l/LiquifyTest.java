package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FarWanderings;
import com.github.laxika.magicalvibes.cards.g.Gloomdrifter;
import com.github.laxika.magicalvibes.cards.i.Insist;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.cards.o.ObsessiveSearch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Liquify.class, FarWanderings.class, Gloomdrifter.class, ObsessiveSearch.class,
        Insist.class, NantukoShade.class})
class LiquifyTest extends BaseCardTest {

    @Test
    void canTargetSpellWithManaValueThreeOrLess() {
        FarWanderings farWanderings = new FarWanderings();
        harness.castFromHand(player1, farWanderings, "{2}{G}");

        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, farWanderings.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
    }

    @Test
    void cannotTargetSpellWithManaValueFour() {
        Gloomdrifter gloomdrifter = new Gloomdrifter();
        harness.castFromHand(player1, gloomdrifter, "{3}{B}");

        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, gloomdrifter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersAndExilesTargetSpellWithManaValueThreeOrLess() {
        ObsessiveSearch obsessiveSearch = new ObsessiveSearch();
        harness.castFromHand(player1, obsessiveSearch, "{U}");

        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, obsessiveSearch.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Obsessive Search"));
        harness.assertNotInGraveyard(player1, "Obsessive Search");
        harness.assertNotOnBattlefield(player1, "Obsessive Search");
        harness.assertInGraveyard(player2, "Liquify");
    }

    @Test
    void canCounterAndExileOwnSpellAtManaValueThree() {
        FarWanderings spell = new FarWanderings();
        harness.castFromHand(player1, spell, "{2}{G}");
        harness.setHand(player1, List.of(new Liquify()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, spell.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        harness.assertNotInGraveyard(player1, "Far Wanderings");
        harness.assertInGraveyard(player1, "Liquify");
    }

    @Test
    void doesNothingWhenTargetWasAlreadyCountered() {
        ObsessiveSearch spell = new ObsessiveSearch();
        harness.castFromHand(player1, spell, "{U}");
        harness.setHand(player2, List.of(new Liquify(), new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castInstant(player2, 0, spell.getId());
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getId().equals(spell.getId()))
                .hasSize(1);
        harness.assertNotInGraveyard(player1, "Obsessive Search");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Liquify"))
                .hasSize(2);
    }

    @Test
    void doesNotExileSpellThatCannotBeCountered() {
        harness.setLibrary(player1, List.of(new FarWanderings()));
        harness.castFromHand(player1, new Insist(), "{G}");
        harness.passBothPriorities();

        NantukoShade spell = new NantukoShade();
        harness.castFromHand(player1, spell, "{B}{B}");
        harness.setHand(player2, List.of(new Liquify()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        harness.assertNotInGraveyard(player1, "Nantuko Shade");
        harness.assertInGraveyard(player2, "Liquify");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nantuko Shade");
    }
}
