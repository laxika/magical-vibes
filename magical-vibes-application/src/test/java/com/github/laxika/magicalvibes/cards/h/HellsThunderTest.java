package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellsThunder.class, ResoundingWave.class})
@DisplayName("Hell's Thunder")
class HellsThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        harness.addToBattlefield(player1, new HellsThunder());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth returns Hell's Thunder to the battlefield with haste")
    void unearthReturnsWithHaste() {
        HellsThunder card = new HellsThunder();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Hell's Thunder");
        assertThat(perm.getPersistentGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearthed Hell's Thunder is exiled at the next end step")
    void unearthExiledAtEndStep() {
        HellsThunder card = new HellsThunder();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hell's Thunder"));
    }

    @Test
    @DisplayName("Sacrifices itself during the opponent's end step too")
    void sacrificesDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new HellsThunder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthCannotBeActivatedAtEndStep() {
        harness.setGraveyard(player1, List.of(new HellsThunder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hell's Thunder");
        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth cannot be activated during the opponent's turn")
    void unearthCannotBeActivatedDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new HellsThunder()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth requires five mana including red")
    void unearthCannotBeActivatedWithOnlyFourMana() {
        harness.setGraveyard(player1, List.of(new HellsThunder()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }

    @Test
    @DisplayName("Unearth returns only the activated copy")
    void unearthReturnsOnlyActivatedCopy() {
        HellsThunder first = new HellsThunder();
        HellsThunder second = new HellsThunder();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hell's Thunder").getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Hell's Thunder")).isEqualTo(1);
    }

    @Test
    @DisplayName("Bouncing an unearthed Hell's Thunder exiles it instead")
    void bouncingUnearthedCreatureExilesIt() {
        HellsThunder card = new HellsThunder();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, findPermanent(player1, "Hell's Thunder").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertNotInHand(player1, "Hell's Thunder");
        harness.assertNotInGraveyard(player1, "Hell's Thunder");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Unearth cannot be activated while a spell is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new HellsThunder()));
        harness.castFromHand(player1, new HellsThunder(), "{1}{R}{R}");
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hell's Thunder");
    }
}
