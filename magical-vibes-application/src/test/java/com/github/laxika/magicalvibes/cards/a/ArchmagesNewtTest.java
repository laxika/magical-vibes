package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchmagesNewt.class, Shock.class})
class ArchmagesNewtTest extends BaseCardTest {

    @Test
    @DisplayName("A saddled Archmage's Newt grants free flashback")
    void saddledGrantsFreeFlashback() {
        Shock shock = new Shock();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setSaddled(true);
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shock));

        resolveCombatAndChoose(shock);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An unsaddled Archmage's Newt grants flashback for the card's mana cost")
    void unsaddledGrantsNormalFlashbackCost() {
        Shock shock = new Shock();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shock));

        resolveCombatAndChoose(shock);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void resolveCombatAndChoose(Shock shock) {
        resolveCombat();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        resolveAllTriggers();
    }
}
