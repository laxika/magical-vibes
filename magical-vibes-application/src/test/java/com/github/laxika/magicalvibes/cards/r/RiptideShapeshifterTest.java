package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideShapeshifter.class, AvianChangeling.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class RiptideShapeshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a creature type, finds a matching creature, and shuffles the other reveals")
    void findsCreatureOfChosenType() {
        Permanent shapeshifter = addCreatureReady(player1, new RiptideShapeshifter());
        Card nonmatching = new HillGiant();
        Card matching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching, matching));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shapeshifter);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matching.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonmatching);
    }

    @Test
    @DisplayName("A Changeling is a creature of the chosen type")
    void changelingMatchesChosenType() {
        addCreatureReady(player1, new RiptideShapeshifter());
        Card changeling = new AvianChangeling();
        harness.setLibrary(player1, List.of(changeling));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(changeling.getId()));
    }

    @Test
    @DisplayName("The ability resolves without finding a creature when the library has none of the chosen type")
    void noMatchingCreature() {
        addCreatureReady(player1, new RiptideShapeshifter());
        Card nonmatching = new Forest();
        harness.setLibrary(player1, List.of(nonmatching));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(nonmatching.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonmatching);
    }
}
