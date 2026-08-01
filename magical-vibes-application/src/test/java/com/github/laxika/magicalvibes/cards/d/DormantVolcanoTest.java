package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DormantVolcanoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new Mountain());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        Permanent volcano = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dormant Volcano"))
                .findFirst().orElseThrow();
        assertThat(volcano.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Mountain")
    void autoSacrificesWithoutUntappedMountain() {
        harness.addToBattlefield(player1, new Mountain());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Dormant Volcano");
        harness.assertInGraveyard(player1, "Dormant Volcano");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Accepting returns an untapped Mountain and keeps Dormant Volcano")
    void acceptReturnsMountainAndKeepsVolcano() {
        harness.addToBattlefield(player1, new Mountain());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Dormant Volcano");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Mountain"))).isTrue();
    }

    @Test
    @DisplayName("Accepting with two untapped Mountains lets controller choose which to return")
    void acceptWithTwoMountainsChoosesOne() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID mountainId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(mountainId));

        harness.assertOnBattlefield(player1, "Dormant Volcano");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain")).count()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Mountain")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining sacrifices Dormant Volcano and keeps the Mountain")
    void declineSacrificesVolcano() {
        harness.addToBattlefield(player1, new Mountain());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Dormant Volcano");
        harness.assertInGraveyard(player1, "Dormant Volcano");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {R}")
    void manaAbilityAddsColorlessAndRed() {
        harness.addToBattlefield(player1, new DormantVolcano());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new DormantVolcano()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
