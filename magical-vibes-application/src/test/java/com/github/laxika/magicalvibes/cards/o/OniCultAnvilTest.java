package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OniCultAnvil.class, Shatter.class, Spellbook.class})
@DisplayName("Oni-Cult Anvil")
class OniCultAnvilTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Construct when an artifact leaves during your turn")
    void createsConstructOnceEachTurn() {
        harness.addToBattlefield(player1, new OniCultAnvil());
        Permanent firstBook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent secondBook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new Shatter(), new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, firstBook.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findConstructs()).hasSize(1);

        harness.castInstant(player1, 0, secondBook.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findConstructs()).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an artifact leaves during an opponent's turn")
    void doesNotTriggerDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new OniCultAnvil());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, book.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findConstructs()).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing an artifact deals damage and gains life")
    void sacrificesArtifactForDamageAndLife() {
        harness.addToBattlefield(player1, new OniCultAnvil());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, book.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findConstructs()).hasSize(1);
    }

    @Test
    @DisplayName("The Anvil creates a Construct when it sacrifices itself")
    void sacrificesItselfAndCreatesConstruct() {
        harness.addToBattlefield(player1, new OniCultAnvil());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Oni-Cult Anvil");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findConstructs()).hasSize(1);
    }

    private List<Permanent> findConstructs() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().hasType(CardType.ARTIFACT))
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CONSTRUCT))
                .toList();
    }
}
