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

        harness.castAndResolveSorcery(player1, 0, 2);

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

    @Test
    void acceptingOneMilledSpellDoesNotAllowCastingAnother() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SummonsOfSaruman()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void mayDeclineCastingAndLeavesMilledSpellInGraveyard() {
        Shock milled = new Shock();
        Shock alreadyInGraveyard = new Shock();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player1, List.of(new SummonsOfSaruman()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled, alreadyInGraveyard);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroXMillsNothingAndNewZeroToughnessArmyDies() {
        Shock libraryCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SummonsOfSaruman()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castsMilledSorceryWithManaValueExactlyX() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        GrizzlyBears drawnFirst = new GrizzlyBears();
        GrizzlyBears drawnSecond = new GrizzlyBears();
        harness.setLibrary(player1, List.of(spell, new GrizzlyBears(), new GrizzlyBears(),
                drawnFirst, drawnSecond));
        harness.setHand(player1, List.of(new SummonsOfSaruman()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }
}
