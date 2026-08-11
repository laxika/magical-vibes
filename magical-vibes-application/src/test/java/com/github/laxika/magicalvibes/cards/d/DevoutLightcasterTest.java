package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevoutLightcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target black permanent")
    void etbExilesTargetBlackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, blackPermanent());

        castLightcaster(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Black Permanent");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Black Permanent"));
    }

    @Test
    @DisplayName("Cannot target a nonblack permanent")
    void cannotTargetNonblackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DevoutLightcaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent lightcaster = harness.addToBattlefieldAndReturn(player1, new DevoutLightcaster());
        lightcaster.setSummoningSick(false);
        lightcaster.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, blackCreature());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent lightcaster = harness.addToBattlefieldAndReturn(player2, new DevoutLightcaster());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, lightcaster.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLightcaster(UUID targetId) {
        harness.setHand(player1, List.of(new DevoutLightcaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Card blackPermanent() {
        Card card = new Card();
        card.setName("Black Permanent");
        card.setType(CardType.ENCHANTMENT);
        card.setColor(CardColor.BLACK);
        return card;
    }

    private Card blackCreature() {
        Card card = new Card();
        card.setName("Black Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
