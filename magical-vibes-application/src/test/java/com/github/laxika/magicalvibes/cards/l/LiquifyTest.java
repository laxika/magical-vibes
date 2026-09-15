package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FarWanderings;
import com.github.laxika.magicalvibes.cards.g.Gloomdrifter;
import com.github.laxika.magicalvibes.cards.o.ObsessiveSearch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Liquify.class, FarWanderings.class, Gloomdrifter.class, ObsessiveSearch.class})
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
        harness.castInstant(player2, 0, obsessiveSearch.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Obsessive Search"));
        harness.assertNotInGraveyard(player1, "Obsessive Search");
        harness.assertNotOnBattlefield(player1, "Obsessive Search");
        harness.assertInGraveyard(player2, "Liquify");
    }
}
