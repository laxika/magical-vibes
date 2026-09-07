package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoldDemon.class, Swamp.class})
class MoldDemonTest extends BaseCardTest {

    private long swampsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Swamp"))
                .count();
    }

    private void castMoldDemon() {
        harness.setHand(player1, List.of(new MoldDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than two Swamps")
    void autoSacrificesWithoutTwoSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        castMoldDemon();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mold Demon");
        harness.assertInGraveyard(player1, "Mold Demon");
        assertThat(swampsControlledBy(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting with exactly two Swamps sacrifices both and keeps Mold Demon")
    void acceptWithExactlyTwoSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        castMoldDemon();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(swampsControlledBy(player1.getId())).isZero();
        harness.assertOnBattlefield(player1, "Mold Demon");
    }

    @Test
    @DisplayName("Accepting with extra Swamps lets the controller choose two")
    void acceptWithExtraSwampsChoosesTwo() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        castMoldDemon();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> swampIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Swamp"))
                .map(permanent -> permanent.getId())
                .limit(2)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, swampIds);

        assertThat(swampsControlledBy(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Mold Demon");
    }

    @Test
    @DisplayName("Declining sacrifices Mold Demon and keeps the Swamps")
    void declineSacrificesMoldDemon() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        castMoldDemon();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Mold Demon");
        harness.assertInGraveyard(player1, "Mold Demon");
        assertThat(swampsControlledBy(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Swamps do not satisfy the requirement")
    void opponentSwampsDoNotCount() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        castMoldDemon();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mold Demon");
        harness.assertInGraveyard(player1, "Mold Demon");
        assertThat(swampsControlledBy(player2.getId())).isEqualTo(2);
    }
}
