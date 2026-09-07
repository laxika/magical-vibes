package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationWarship.class, GrizzlyBears.class, Oxidize.class})
class FireNationWarshipTest extends BaseCardTest {

    @Test
    @DisplayName("When Fire Nation Warship dies, its controller creates a Clue")
    void deathTriggerCreatesClueForController() {
        Permanent warship = addWarshipReady(player1);

        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, warship.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fire Nation Warship");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 animates Fire Nation Warship and taps the crew")
    void crewAnimatesWarshipAndTapsCrew() {
        Permanent warship = addWarshipReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, warship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addWarshipReady(Player player) {
        Permanent warship = new Permanent(new FireNationWarship());
        warship.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(warship);
        return warship;
    }
}
