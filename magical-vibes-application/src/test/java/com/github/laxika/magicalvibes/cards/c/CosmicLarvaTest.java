package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicLarva.class, CacklingImp.class, Forest.class, Mountain.class})
class CosmicLarvaTest extends BaseCardTest {

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }

    @Test
    @DisplayName("With fewer than two lands, the upkeep trigger sacrifices Cosmic Larva")
    void insufficientLandsSacrificeLarva() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Cosmic Larva");
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("With two lands, the upkeep trigger asks whether to sacrifice them")
    void promptsWhenTwoLandsAreAvailable() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly two lands sacrifices both and keeps Cosmic Larva")
    void acceptsSacrificeOfTwoLands() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(landCount(player1)).isZero();
        harness.assertOnBattlefield(player1, "Cosmic Larva");
    }

    @Test
    @DisplayName("With more than two lands, the controller chooses exactly two to sacrifice")
    void choosesTwoOfMoreThanTwoLands() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new Forest());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstMountain.getId(), secondMountain.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(landCount(player1)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Cosmic Larva");
    }

    @Test
    @DisplayName("Declining to sacrifice lands sacrifices Cosmic Larva instead")
    void declinesSacrificeOfLands() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cosmic Larva");
        assertThat(landCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonland permanents do not satisfy the upkeep cost")
    void nonlandPermanentsDoNotCountAsLands() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player1, new CacklingImp());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cosmic Larva");
        harness.assertOnBattlefield(player1, "Cackling Imp");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands controlled by an opponent do not satisfy the upkeep cost")
    void opponentLandsDoNotCount() {
        harness.addToBattlefield(player1, new CosmicLarva());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cosmic Larva");
        assertThat(landCount(player2)).isEqualTo(2);
    }
}
