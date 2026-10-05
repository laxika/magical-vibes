package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SzarekhTheSilentKing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Lokhust Heavy Destroyer")
@CardUsed({LokhustHeavyDestroyer.class, SzarekhTheSilentKing.class})
class LokhustHeavyDestroyerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice a creature")
    void etbMakesEachPlayerSacrifice() {
        harness.addToBattlefield(player2, new SzarekhTheSilentKing());
        harness.setHand(player1, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lokhust Heavy Destroyer");
        harness.assertInGraveyard(player2, "Szarekh, the Silent King");
    }

    @Test
    @DisplayName("Unearth returns Lokhust Heavy Destroyer with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.addToBattlefield(player1, new SzarekhTheSilentKing());
        harness.setGraveyard(player1, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lokhust Heavy Destroyer").getGrantedKeywords())
                .contains(com.github.laxika.magicalvibes.model.Keyword.HASTE);

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Szarekh, the Silent King")));
        harness.assertInGraveyard(player1, "Szarekh, the Silent King");
        harness.assertOnBattlefield(player1, "Lokhust Heavy Destroyer");

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lokhust Heavy Destroyer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Lokhust Heavy Destroyer"));
    }

    @Test
    @DisplayName("Players choose their own creatures before either sacrifice happens")
    void bothPlayersChooseBeforeSacrificing() {
        harness.addToBattlefield(player1, new SzarekhTheSilentKing());
        harness.addToBattlefield(player2, new SzarekhTheSilentKing());
        harness.addToBattlefield(player2, new LokhustHeavyDestroyer());
        harness.setHand(player1, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Szarekh, the Silent King")));

        harness.assertOnBattlefield(player1, "Szarekh, the Silent King");
        harness.assertOnBattlefield(player2, "Lokhust Heavy Destroyer");
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Lokhust Heavy Destroyer")));

        harness.assertInGraveyard(player1, "Szarekh, the Silent King");
        harness.assertInGraveyard(player2, "Lokhust Heavy Destroyer");
        harness.assertOnBattlefield(player1, "Lokhust Heavy Destroyer");
        harness.assertOnBattlefield(player2, "Szarekh, the Silent King");
    }

    @Test
    @DisplayName("Unearth's ETB sacrifice exiles the Destroyer when it is the only creature")
    void unearthedDestroyerIsExiledInsteadOfDying() {
        harness.setGraveyard(player1, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lokhust Heavy Destroyer");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lokhust Heavy Destroyer");
        harness.assertNotInGraveyard(player1, "Lokhust Heavy Destroyer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lokhust Heavy Destroyer"));
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lokhust Heavy Destroyer");
    }

    @Test
    @DisplayName("Unearth cannot be activated on the opponent's turn")
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new LokhustHeavyDestroyer()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Lokhust Heavy Destroyer");
    }

    @Test
    @DisplayName("Unearth cannot be activated while a spell is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new LokhustHeavyDestroyer()));
        harness.setHand(player1, List.of(new SzarekhTheSilentKing()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lokhust Heavy Destroyer");
    }
}
