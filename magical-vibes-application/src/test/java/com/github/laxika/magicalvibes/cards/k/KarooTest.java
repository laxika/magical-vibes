package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
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

class KarooTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        Permanent karoo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Karoo"))
                .findFirst().orElseThrow();
        assertThat(karoo.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Plains")
    void autoSacrificesWithoutUntappedPlains() {
        harness.addToBattlefield(player1, new Plains());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addToBattlefield(player1, new Island());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Karoo");
        harness.assertInGraveyard(player1, "Karoo");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Accepting returns an untapped Plains and keeps Karoo")
    void acceptReturnsPlainsAndKeepsKaroo() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Karoo");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Plains"))).isTrue();
    }

    @Test
    @DisplayName("Accepting with two untapped Plains lets controller choose which to return")
    void acceptWithTwoPlainsChoosesOne() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID plainsId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plains"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(plainsId));

        harness.assertOnBattlefield(player1, "Karoo");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plains")).count()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Plains")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining sacrifices Karoo and keeps the Plains")
    void declineSacrificesKaroo() {
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Karoo");
        harness.assertInGraveyard(player1, "Karoo");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {W}")
    void manaAbilityAddsColorlessAndWhite() {
        harness.addToBattlefield(player1, new Karoo());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new Karoo()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
