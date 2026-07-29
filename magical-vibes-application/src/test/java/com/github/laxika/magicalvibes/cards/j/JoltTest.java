package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoltTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature and schedules a draw at the next upkeep")
    void tapsCreatureAndSchedulesDraw() {
        harness.addToBattlefield(player2, new HillGiant());
        castJolt(harness.getPermanentId(player2, "Hill Giant"));

        assertThat(findPermanent(player2, "Hill Giant").isTapped()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        findPermanent(player2, "Hill Giant").tap();

        castJolt(harness.getPermanentId(player2, "Hill Giant"));

        assertThat(findPermanent(player2, "Hill Giant").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a land")
    void tapsLand() {
        harness.addToBattlefield(player2, new Forest());
        castJolt(harness.getPermanentId(player2, "Forest"));

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an artifact")
    void tapsArtifact() {
        harness.addToBattlefield(player2, new AngelsFeather());
        castJolt(harness.getPermanentId(player2, "Angel's Feather"));

        assertThat(findPermanent(player2, "Angel's Feather").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new HillGiant());
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new Jolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Does not draw immediately on resolution")
    void doesNotDrawImmediately() {
        harness.addToBattlefield(player2, new HillGiant());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castJolt(harness.getPermanentId(player2, "Hill Giant"));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castJolt(UUID targetId) {
        harness.setHand(player1, List.of(new Jolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
