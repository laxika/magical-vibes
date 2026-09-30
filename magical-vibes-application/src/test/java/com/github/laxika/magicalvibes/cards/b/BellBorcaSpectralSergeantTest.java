package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BellBorcaSpectralSergeant.class)
class BellBorcaSpectralSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Power starts at zero and toughness is five")
    void startsAtZeroPowerAndFiveToughness() {
        Permanent bell = addReadyBell(player1);

        assertThat(gqs.getEffectivePower(gd, bell)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bell)).isEqualTo(5);
    }

    @Test
    @DisplayName("Notes the greatest mana value of cards put into exile this turn")
    void notesGreatestManaValueOfExiledCards() {
        Permanent bell = addReadyBell(player1);
        Card low = cardWithManaCost("Low", "{2}");
        Card high = cardWithManaCost("High", "{5}");

        harness.inMutationScope(() -> {
            gd.addToExile(player2.getId(), low);
            gd.addToExile(player1.getId(), high);
        });

        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bell)).isEqualTo(5);
    }

    @Test
    @DisplayName("Upkeep trigger exiles the top card with normal-cost play permission")
    void upkeepExilesTopCardWithNormalCostPermission() {
        Card top = cardWithManaCost("Top", "{4}");
        harness.setLibrary(player1, List.of(top));
        Permanent bell = addReadyBell(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(gqs.getEffectivePower(gd, bell)).isEqualTo(4);
    }

    private Permanent addReadyBell(Player player) {
        Permanent bell = new Permanent(new BellBorcaSpectralSergeant());
        bell.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bell);
        return bell;
    }

    private Card cardWithManaCost(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        return card;
    }
}
