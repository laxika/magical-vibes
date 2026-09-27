package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TombFortress.class, Forest.class, GrizzlyBears.class})
class TombFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for black mana")
    void entersTappedAndTapsForBlackMana() {
        harness.setHand(player1, List.of(new TombFortress()));
        harness.playLand(player1, 0);
        Permanent fortress = findPermanent(player1, "Tomb Fortress");

        assertThat(fortress.isTapped()).isTrue();
        fortress.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles itself, mills four, and returns a creature from the graveyard")
    void exilesMillsAndReturnsCreature() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new TombFortress());
        fortress.untap();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(fortress.getCard().getId()));
    }
}
