package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WretchedBanquetTest extends BaseCardTest {

    // ===== Least power: destroyed =====

    @Test
    @DisplayName("Destroys a creature that uniquely has the least power")
    void destroysUniqueLeastPower() {
        UUID target = addCreature(player2, new GrizzlyBears()); // power 2 (least)
        addCreature(player2, new HillGiant()); // power 3
        castWretchedBanquet(target);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    // ===== Tied for least power: still destroyed =====

    @Test
    @DisplayName("Destroys a creature tied for least power")
    void destroysTiedForLeastPower() {
        UUID target = addCreature(player2, new GrizzlyBears()); // power 2
        addCreature(player2, new GiantSpider()); // power 2 (tied least)
        addCreature(player2, new HillGiant()); // power 3
        castWretchedBanquet(target);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Not least power: nothing happens =====

    @Test
    @DisplayName("Does nothing when the target does not have the least power")
    void survivesWhenNotLeastPower() {
        UUID target = addCreature(player2, new HillGiant()); // power 3
        addCreature(player2, new GrizzlyBears()); // power 2 (the least)
        castWretchedBanquet(target);

        // Hill Giant is not tied for least power, so the destroy is skipped at resolution.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new WretchedBanquet()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private UUID addCreature(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(owner, card);
        return harness.getPermanentId(owner, card.getName());
    }

    private void castWretchedBanquet(UUID targetId) {
        harness.setHand(player1, List.of(new WretchedBanquet()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }
}
