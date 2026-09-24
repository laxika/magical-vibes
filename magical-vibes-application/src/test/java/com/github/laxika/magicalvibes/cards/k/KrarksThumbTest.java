package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.FieryGambit;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrarksThumb.class, FieryGambit.class, AlphaMyr.class})
class KrarksThumbTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a logical coin flip with two flips and ignores one")
    void replacesCoinFlip() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player1, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).anyMatch(log -> log.contains(
                "coin flip for Fiery Gambit (flipped 2 coins and ignored 1)"));
    }

    @Test
    @DisplayName("A Thumb controlled by the opponent does not replace your coin flip")
    void onlyTheFlippersControllerBenefitsFromThumb() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new KrarksThumb());
        castFieryGambit(target);

        assertThat(coinFlipLogs()).isNotEmpty()
                .noneMatch(log -> log.contains("(flipped 2 coins and ignored 1)"));
    }

    private void castFieryGambit(Permanent target) {
        harness.setHand(player1, List.of(new FieryGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    private List<String> coinFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fiery Gambit"))
                .toList();
    }
}
