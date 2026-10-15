package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlumesOfPeace.class, AssaultZeppelid.class, AzoriusSignet.class})
class PlumesOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forecast taps a creature and keeps Plumes of Peace in hand")
    void forecastTapsTargetAndKeepsSourceInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plumes);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires its controller's upkeep")
    void forecastRequiresControllerUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plumes);
    }

    @Test
    @DisplayName("Forecast can target only a creature")
    void forecastRequiresCreatureTarget() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plumes);
    }

    @Test
    @DisplayName("Plumes of Peace can enchant only a creature")
    void auraRequiresCreatureTarget() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed throughout upkeep")
    void forecastSourceRemainsRevealedUntilUpkeepEnds() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes, new AzoriusSignet()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.clearMessages();
        harness.publishState();

        List<String> states = harness.getConn2().getMessagesContaining("\"opponentHand\"");
        assertThat(states).isNotEmpty();
        var revealed = new JacksonConfig().objectMapper().readTree(states.getLast()).get("opponentHand");
        assertThat(revealed.size()).isEqualTo(1);
        assertThat(revealed.get(0).get("id").asText()).isEqualTo(plumes.getId().toString());

        harness.passUntil(player1, TurnStep.DRAW);
        harness.clearMessages();
        harness.publishState();

        states = harness.getConn2().getMessagesContaining("\"opponentHand\"");
        assertThat(states).isNotEmpty();
        assertThat(new JacksonConfig().objectMapper().readTree(states.getLast()).get("opponentHand").size())
                .isZero();
    }

    @Test
    @DisplayName("Separate copies may each forecast during the same upkeep")
    void separateCopiesCanForecastInSameTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PlumesOfPeace(), new PlumesOfPeace()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, first.getId());
        harness.activateHandAbility(player1, 1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast cannot be activated during its owner's main phase")
    void forecastCannotBeActivatedInMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Aura does not tap the opponent's creature and prevents only that creature from untapping")
    void auraLocksOpponentsCreatureWithoutTappingIt() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        assertThat(enchanted.isTapped()).isFalse();
        enchanted.tap();
        other.tap();

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }
}
