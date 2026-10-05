package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({Junktown.class})
class JunktownTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent junktown = harness.addToBattlefieldAndReturn(player1, new Junktown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(junktown.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Junktown creates three Junk tokens")
    void sacrificesToCreateJunkTokens() {
        harness.addToBattlefield(player1, new Junktown());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Junk")).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Junktown");
        harness.assertInGraveyard(player1, "Junktown");
    }

    @Test
    void canCreateJunkDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new Junktown());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Junktown");
        harness.assertInGraveyard(player1, "Junktown");
        assertThat(countPermanents(player1, "Junk")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(3);
    }

    @Test
    void newlyCreatedJunkCanExileAndPlayALand() {
        createJunk();
        Junktown topCard = new Junktown();
        Junktown nextCard = new Junktown();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Junktown");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void junkCannotBeActivatedDuringUpkeep() {
        createJunk();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(3);
        assertThat(findPermanent(player1, "Junk").isTapped()).isFalse();
    }

    @Test
    void junkCanBeSacrificedWithAnEmptyLibrary() {
        createJunk();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void createJunk() {
        harness.addToBattlefield(player1, new Junktown());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();
    }
}
