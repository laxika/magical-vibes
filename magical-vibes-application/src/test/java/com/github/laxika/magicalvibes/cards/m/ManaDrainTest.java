package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HeadlessHorseman;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaDrain.class, HeadlessHorseman.class, HolyDay.class})
class ManaDrainTest extends BaseCardTest {

    private Card counterSpell(Card spell, String manaCost) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, spell, manaCost);

        harness.setHand(player2, List.of(new ManaDrain()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        return spell;
    }

    @Test
    @DisplayName("Counters a creature spell and adds colorless mana equal to its mana value at the next main phase")
    void countersAndAddsManaAtNextMainPhase() {
        Card horseman = counterSpell(new HeadlessHorseman(), "{2}{B}");

        harness.assertNotOnBattlefield(player1, horseman.getName());
        harness.assertInGraveyard(player1, horseman.getName());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters a noncreature spell and still uses its mana value for the delayed mana")
    void countersNoncreatureSpell() {
        Card holyDay = counterSpell(new HolyDay(), "{W}");

        harness.assertNotOnBattlefield(player1, holyDay.getName());
        harness.assertInGraveyard(player1, holyDay.getName());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
