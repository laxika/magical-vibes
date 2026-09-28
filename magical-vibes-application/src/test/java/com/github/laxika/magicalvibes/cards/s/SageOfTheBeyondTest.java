package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SageOfTheBeyond.class, GrizzlyBears.class})
class SageOfTheBeyondTest extends BaseCardTest {

    @Test
    void reducesTheCostOfSpellsCastFromOutsideTheHand() {
        harness.addToBattlefield(player1, new SageOfTheBeyond());

        GrizzlyBears handCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(handCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        GrizzlyBears graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        gd.graveyardPlayPermissions.put(graveyardCreature.getId(), player1.getId());
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canBeForetoldAndCastOnALaterTurn() {
        SageOfTheBeyond sage = new SageOfTheBeyond();
        harness.setHand(player1, List.of(sage));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(sage.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(sage.getId());

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, sage.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sage of the Beyond");
    }
}
