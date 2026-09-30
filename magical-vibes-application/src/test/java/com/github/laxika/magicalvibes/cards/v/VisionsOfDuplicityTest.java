package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfDuplicity.class, GrizzlyBears.class, HillGiant.class, EdgarMarkov.class})
class VisionsOfDuplicityTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when both targets have the same controller")
    void doesNothingWhenBothTargetsHaveSameController() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Rejects a creature controlled by the spell's caster")
    void rejectsCreatureYouControl() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new VisionsOfDuplicity()));
        addManaForNormalCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    @DisplayName("Flashback cost is reduced by the greatest owned commander")
    void flashbackUsesCommanderManaValue() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        VisionsOfDuplicity spell = new VisionsOfDuplicity();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void addManaForNormalCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
