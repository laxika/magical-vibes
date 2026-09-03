package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantStreetArtist.class, CounselOfTheSoratami.class, Twincast.class})
class ErrantStreetArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell you control that wasn't cast")
    void copiesSpellThatWasNotCast() {
        addReadyErrant(player2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();

        UUID copyId = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .map(entry -> entry.getCard().getId())
                .findFirst()
                .orElseThrow();
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 0, null, copyId);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(copy -> copy.getControllerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("Cannot target a spell that was cast")
    void cannotTargetCastSpell() {
        addReadyErrant(player1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyErrant(Player player) {
        Permanent errant = new Permanent(new ErrantStreetArtist());
        errant.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(errant);
    }
}
