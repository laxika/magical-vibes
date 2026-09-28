package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiaraOfTheFlamingFist.class, GrizzlyBears.class})
class LiaraOfTheFlamingFistTest extends BaseCardTest {

    @Test
    void boostsCreaturesSharingANameAtBeginningOfEachCombat() {
        Permanent firstLiara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent secondLiara = addCreatureReady(player1, namedLiaraCreature());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(firstLiara.getPowerModifier()).isEqualTo(1);
        assertThat(secondLiara.getPowerModifier()).isEqualTo(1);
        assertThat(ownBears.getPowerModifier()).isZero();
        assertThat(opposingBears.getPowerModifier()).isZero();
    }

    @Test
    void boostsCreaturesSharingANameWithACreatureCardInTheGraveyard() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);

        assertThat(liara.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void combatBoostWearsOffAtEndOfTurn() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        harness.setGraveyard(player1, List.of(new LiaraOfTheFlamingFist()));

        advanceToCombat(player1);
        assertThat(liara.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gd.interaction.clearAwaitingInput();
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(liara.getPowerModifier()).isZero();
    }

    @Test
    void activatedAbilityGrantsFirstStrikeAndDoubleTeamOnlyOnce() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void activatedAbilityCannotTargetTheSourceOrAnOpponentCreature() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, liara.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken creature you control");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private Card namedLiaraCreature() {
        Card card = new Card();
        card.setName("Liara of the Flaming Fist");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
