package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoatnapperTest extends BaseCardTest {

    private static Card goat(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(0);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.GOAT));
        return card;
    }

    private void castGoatnapper(UUID targetId) {
        harness.setHand(player1, List.of(new Goatnapper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the Goat")
    void etbTriggersOnStack() {
        harness.addToBattlefield(player2, goat("Mtenda Goat"));
        UUID targetId = harness.getPermanentId(player2, "Mtenda Goat");
        castGoatnapper(targetId);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goatnapper"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Untaps, steals until end of turn and grants haste to the Goat")
    void stealsUntapsAndGrantsHaste() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, goat("Mtenda Goat"));
        goat.tap();
        castGoatnapper(goat.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(goat.isTapped()).isFalse();
        assertThat(goat.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(goat.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(goat.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(goat.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, goat("Mtenda Goat"));
        castGoatnapper(goat.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goat.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(goat.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(goat.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(goat.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Goat creature")
    void cannotTargetNonGoat() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Goatnapper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No ETB trigger when cast with no Goat to target")
    void noTriggerWithoutGoat() {
        castGoatnapper(null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goatnapper"));
        assertThat(gd.stack).isEmpty();
    }
}
