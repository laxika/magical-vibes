package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointWithAffliction.class, Forest.class, GrayOgre.class, HillGiant.class})
class AnointWithAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with mana value 3 or less")
    void exilesLowManaValueCreature() {
        harness.addToBattlefield(player2, new GrayOgre());
        castOn(harness.getPermanentId(player2, "Gray Ogre"));

        harness.assertNotOnBattlefield(player2, "Gray Ogre");
        harness.assertNotInGraveyard(player2, "Gray Ogre");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Gray Ogre"));
    }

    @Test
    @DisplayName("May target a creature with mana value greater than 3 when it is not corrupted")
    void highManaValueCreatureIsLegalTargetWithoutCorrupted() {
        harness.addToBattlefield(player2, new HillGiant());
        castOn(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Anoint with Affliction");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Corrupted exiles a creature with mana value greater than 3")
    void corruptedExilesHighManaValueCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castOn(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castOn(UUID targetId) {
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
