package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.k.KamiOfThePalaceFields;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeWhoHungers.class, LanternKami.class, KamiOfOldStone.class, Forest.class,
        RendSpirit.class, KamiOfThePalaceFields.class})
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

    private void killHeWhoHungers() {
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "He Who Hungers"));
    }

    @Test
    @DisplayName("Sacrificing a Spirit makes the target opponent discard the chosen card")
    void abilityDiscardsChosenCard() {
        readyHeWhoHungers();
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        harness.setHand(player2, List.of(new KamiOfOldStone(), new Forest()));
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
        harness.assertInHand(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("He Who Hungers can be sacrificed to pay for its own ability")
    void canSacrificeItself() {
        readyHeWhoHungers();
        harness.setHand(player2, List.of(new KamiOfOldStone()));
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
    @DisplayName("The ability can target only an opponent")
    void cannotTargetController() {
        readyHeWhoHungers();
        mainPhaseWithMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Soulshift 4 returns a targeted Spirit with mana value 4 or less to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killHeWhoHungers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift 4 cannot target a Spirit with mana value 5 or greater")
    void expensiveSpiritNotTargetable() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card expensiveSpirit = new KamiOfThePalaceFields();
        harness.setGraveyard(player1, List.of(expensiveSpirit));

        killHeWhoHungers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killHeWhoHungers();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift only offers your Spirits with mana value 4 or less")
    void soulshiftFiltersByControllerTypeAndManaValue() {
        harness.addToBattlefield(player1, new HeWhoHungers());
        Card boundarySpirit = new KamiOfOldStone();
        Card nonSpirit = new Forest();
        Card expensiveSpirit = new KamiOfThePalaceFields();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(boundarySpirit, nonSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killHeWhoHungers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(boundarySpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(boundarySpirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Kami of the Palace Fields");
        harness.assertInGraveyard(player2, "Lantern Kami");
    }
}
