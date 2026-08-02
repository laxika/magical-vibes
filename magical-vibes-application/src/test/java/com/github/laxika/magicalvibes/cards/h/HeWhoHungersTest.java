package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeWhoHungersTest extends BaseCardTest {

    private Permanent readyHeWhoHungers() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new HeWhoHungers());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void mainPhaseWithMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Sacrificing a Spirit makes the target opponent discard the chosen card")
    void abilityDiscardsChosenCard() {
        readyHeWhoHungers();
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        mainPhaseWithMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        // Two Spirits on the battlefield → the cost asks which one to sacrifice.
        harness.handlePermanentChosen(player1, kami.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("He Who Hungers can be sacrificed to pay for its own ability")
    void canSacrificeItself() {
        readyHeWhoHungers();
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        mainPhaseWithMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "He Who Hungers");
        harness.assertInGraveyard(player1, "He Who Hungers");
    }

    @Test
    @DisplayName("The ability cannot be activated during the opponent's turn")
    void cannotActivateAtInstantSpeed() {
        readyHeWhoHungers();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Soulshift 4 returns a targeted Spirit with mana value 4 or less to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift 4 cannot target a Spirit with mana value 5 or greater")
    void expensiveSpiritNotTargetable() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card expensiveSpirit = new BattlegroundGeist();
        harness.setGraveyard(player1, new ArrayList<>(List.of(expensiveSpirit)));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
