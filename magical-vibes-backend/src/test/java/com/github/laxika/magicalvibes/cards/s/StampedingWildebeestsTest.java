package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StampedingWildebeestsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Stampeding Wildebeests has correct card properties")
    void hasCorrectProperties() {
        StampedingWildebeests card = new StampedingWildebeests();

        assertThat(card.getName()).isEqualTo("Stampeding Wildebeests");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(5);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(BounceCreatureOnUpkeepEffect.class);
    }

    @Test
    @DisplayName("Triggers only during its controller upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new StampedingWildebeests());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDescription()).contains("Stampeding Wildebeests's upkeep ability");
    }

    @Test
    @DisplayName("Prompt only includes green creatures you control")
    void promptOnlyIncludesGreenCreaturesYouControl() {
        addPermanent(player1, new StampedingWildebeests());
        Permanent greenCreature = addPermanent(player1, new GrizzlyBears());
        Permanent nonGreenCreature = addPermanent(player1, new RagingGoblin());
        Permanent opponentsGreenCreature = addPermanent(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds())
                .contains(greenCreature.getId())
                .doesNotContain(nonGreenCreature.getId())
                .doesNotContain(opponentsGreenCreature.getId());
    }

    @Test
    @DisplayName("Can choose itself when it is the only green creature")
    void canChooseItselfWhenOnlyGreenCreature() {
        Permanent wildebeests = addPermanent(player1, new StampedingWildebeests());
        addPermanent(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).containsExactly(wildebeests.getId());
    }

    @Test
    @DisplayName("Chosen green creature is returned to owner's hand")
    void chosenGreenCreatureReturnedToOwnersHand() {
        addPermanent(player1, new StampedingWildebeests());
        Permanent greenCreature = addPermanent(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, greenCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(greenCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
