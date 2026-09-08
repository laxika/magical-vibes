package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BullseyeDeathDealer.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class BullseyeDeathDealerTest extends BaseCardTest {

    @Test
    @DisplayName("Bullseye's enters ability can sacrifice an artifact and deal 2 damage")
    void entersAbilitySacrificesArtifactAndDealsDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new BullseyeDeathDealer()));
        int lifeBefore = gd.getLife(player2.getId());
        castBullseyeToMayPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice an artifact");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Bullseye's enters ability can discard a nonland card and deal 2 damage")
    void entersAbilityDiscardsNonlandAndDealsDamage() {
        harness.setHand(player1, new ArrayList<>(List.of(new BullseyeDeathDealer(), new Forest(), new GrizzlyBears())));
        int lifeBefore = gd.getLife(player2.getId());
        castBullseyeToMayPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a nonland card");
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Bullseye's sacrifice activation deals 2 damage")
    void sacrificeActivationDealsDamage() {
        addReadyBullseye();
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Bullseye's discard activation discards only a nonland card and deals 2 damage")
    void discardActivationDiscardsNonlandAndDealsDamage() {
        addReadyBullseye();
        harness.setHand(player1, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    private void castBullseyeToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    private Permanent addReadyBullseye() {
        Permanent bullseye = new Permanent(new BullseyeDeathDealer());
        bullseye.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bullseye);
        return bullseye;
    }
}
