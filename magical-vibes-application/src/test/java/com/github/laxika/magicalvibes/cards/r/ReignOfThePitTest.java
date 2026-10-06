package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReignOfThePit.class, GrizzlyBears.class, HillGiant.class})
@DisplayName("Reign of the Pit")
class ReignOfThePitTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices a creature and the caster creates a Demon sized to their total power")
    void createsDemonWithTotalSacrificedPower() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        castReignOfThePit();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("All players choose before the chosen creatures are sacrificed")
    void allPlayersChooseBeforeSacrifice() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new HillGiant());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castReignOfThePit();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(chosen.getId()));

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingCreature.getId()));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Demon").getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("A player without a creature still contributes zero power")
    void playerWithoutCreatureContributesZeroPower() {
        addCreatureReady(player1, new GrizzlyBears());

        castReignOfThePit();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(2);
        assertThat(demon.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The caster creates a flying Demon even when only the opponent sacrifices")
    void casterWithoutCreatureStillCreatesDemon() {
        addCreatureReady(player2, new HillGiant());

        castReignOfThePit();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().isToken()).isTrue();
        assertThat(demon.getCard().getPower()).isEqualTo(3);
        assertThat(demon.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demon, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Demon");
    }

    @Test
    @DisplayName("No creatures produces a zero-toughness Demon that dies")
    void noCreaturesLeavesNoDemon() {
        castReignOfThePit();

        harness.assertNotOnBattlefield(player1, "Demon");
        harness.assertInGraveyard(player1, "Reign of the Pit");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Demon uses modified battlefield power rather than printed power")
    void includesPowerModifiers() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(3);
        addCreatureReady(player2, new HillGiant());

        castReignOfThePit();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(8);
        assertThat(demon.getCard().getToughness()).isEqualTo(8);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Negative sacrificed power reduces the total")
    void negativePowerReducesTotal() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(-3);
        addCreatureReady(player2, new HillGiant());

        castReignOfThePit();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(2);
        assertThat(demon.getCard().getToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    private void castReignOfThePit() {
        harness.castFromHand(player1, new ReignOfThePit(), "{4}{B}{B}");
        harness.passBothPriorities();
    }
}
