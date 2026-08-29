package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HiredClaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyFlamecaller.class, HiredClaw.class})
class ValleyFlamecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts matching combat and noncombat damage")
    void boostsMatchingCombatAndNoncombatDamage() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new HiredClaw());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not boost a creature with an unrelated subtype")
    void doesNotBoostUnrelatedSubtype() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, testCreature(CardSubtype.BIRD));
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Card testCreature(CardSubtype subtype) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(2);
        return card;
    }
}
