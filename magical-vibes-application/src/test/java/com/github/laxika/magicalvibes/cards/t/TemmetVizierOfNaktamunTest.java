package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemmetVizierOfNaktamunTest extends BaseCardTest {

    private Card creatureToken(String name, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(token);
        return card;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    // ===== Beginning-of-combat trigger =====

    @Test
    @DisplayName("Grants +1/+1 and can't be blocked to target creature token you control")
    void boostsAndUnblockableForOwnCreatureToken() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Zombie", true));
        harness.addToBattlefield(player1, new TemmetVizierOfNaktamun());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(token.getPowerModifier()).isEqualTo(1);
        assertThat(token.getToughnessModifier()).isEqualTo(1);
        assertThat(token.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Zombie", true));
        harness.addToBattlefield(player1, new TemmetVizierOfNaktamun());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(token.getPowerModifier()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.getPowerModifier()).isEqualTo(0);
        assertThat(token.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A nontoken creature you control is not a valid target")
    void nontokenCreatureIsNotValidTarget() {
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, creatureToken("Bear", false));
        harness.addToBattlefield(player1, new TemmetVizierOfNaktamun());

        advanceToCombat(player1);

        // No creature token to target — the trigger is skipped, nothing awaits input.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(nontoken.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Zombie", true));
        harness.addToBattlefield(player1, new TemmetVizierOfNaktamun());

        advanceToCombat(player2); // opponent's combat

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(token.getPowerModifier()).isEqualTo(0);
    }

    // ===== Embalm =====

    @Test
    @DisplayName("Embalm exiles the source from the graveyard and creates a white Zombie token copy")
    void embalmCreatesWhiteZombieTokenCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TemmetVizierOfNaktamun()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Temmet, Vizier of Naktamun"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Temmet, Vizier of Naktamun"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }
}
