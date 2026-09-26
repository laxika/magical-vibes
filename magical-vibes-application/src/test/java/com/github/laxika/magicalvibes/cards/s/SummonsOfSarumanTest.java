package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonsOfSaruman.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class SummonsOfSarumanTest extends BaseCardTest {

    @Test
    void amassesOrcsMillsAndCastsEligibleMilledSpellForFree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        CounselOfTheSoratami tooExpensive = new CounselOfTheSoratami();
        Shock eligible = new Shock();
        harness.setLibrary(player1, List.of(tooExpensive, eligible));
        harness.setHand(player1, List.of(new SummonsOfSaruman()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tooExpensive, eligible);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tooExpensive, eligible);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void flashbackExilesXGraveyardCardsAndTheSpell() {
        Card spell = new SummonsOfSaruman();
        Card costCardOne = new GrizzlyBears();
        Card costCardTwo = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell, costCardOne, costCardTwo));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(costCardTwo, costCardOne, spell);
    }
}
