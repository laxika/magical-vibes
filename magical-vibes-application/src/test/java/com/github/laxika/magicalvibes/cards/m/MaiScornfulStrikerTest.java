package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaiScornfulStriker.class, Divination.class, GrizzlyBears.class})
class MaiScornfulStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Makes an opponent lose 2 life when they cast a noncreature spell")
    void opponentCastingNoncreatureSpellLosesLife() {
        harness.addToBattlefield(player1, new MaiScornfulStriker());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Makes its controller lose 2 life when they cast a noncreature spell")
    void controllerCastingNoncreatureSpellLosesLife() {
        harness.addToBattlefield(player1, new MaiScornfulStriker());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when a creature spell is cast")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MaiScornfulStriker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
