package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderOfWhiteclayTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W}{W} and untapping reanimates a low-mana-value creature from the graveyard")
    void reanimatesLowManaValueCreatureAndUntapsSource() {
        Permanent order = addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        // Paying {Q} untapped the source.
        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a creature card with mana value greater than 3")
    void cannotTargetHighManaValueCreature() {
        addTapped(player1, new OrderOfWhiteclay());
        harness.addMana(player1, ManaColor.WHITE, 3);

        Card target = new SerraAngel();
        harness.setGraveyard(player1, List.of(target));

        enterMainWithPriority(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
