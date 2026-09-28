package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadHandNinja.class, GrizzlyBears.class, Forest.class, Reminisce.class})
class UndeadHandNinjaTest extends BaseCardTest {

    @Test
    @DisplayName("One simultaneous creature-card departure makes each opponent lose 1 and you gain 1")
    void triggersOnceForSeveralCreatureCardsLeavingTogether() {
        harness.addToBattlefield(player1, new UndeadHandNinja());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when only a noncreature card leaves your graveyard")
    void doesNotTriggerForNoncreatureCard() {
        harness.addToBattlefield(player1, new UndeadHandNinja());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
