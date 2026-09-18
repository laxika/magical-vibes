package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DwarvenDriller;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaDart.class, DwarvenDriller.class, Mountain.class})
class LavaDartTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void dealsDamageToPlayer() {
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToCreature() {
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenDriller());

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback sacrifices a Mountain and exiles Lava Dart after it resolves")
    void flashbackSacrificesMountainAndExilesSpell() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new LavaDart()));

        harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), mountain.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotInGraveyard(player1, "Lava Dart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lava Dart"));
    }

    @Test
    @DisplayName("Flashback requires sacrificing a Mountain")
    void flashbackRejectsNonMountainSacrifice() {
        Permanent nonMountain = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        harness.setGraveyard(player1, List.of(new LavaDart()));

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), nonMountain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(nonMountain);
    }

    @Test
    @DisplayName("Flashback cannot sacrifice an opponent's Mountain")
    void flashbackRejectsOpponentsMountain() {
        Permanent opponentMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new LavaDart()));

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(
                player1, 0, player2.getId(), opponentMountain.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentMountain);
        harness.assertInGraveyard(player1, "Lava Dart");
    }

    @Test
    @DisplayName("Flashback cannot sacrifice an opponent's Mountain")
    void flashbackRejectsOpponentsMountainSacrifice() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new LavaDart()));

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(mountain);
        harness.assertInGraveyard(player1, "Lava Dart");
    }
}
