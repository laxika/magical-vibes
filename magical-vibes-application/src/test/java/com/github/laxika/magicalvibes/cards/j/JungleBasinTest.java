package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JungleBasin.class, Forest.class, Plains.class})
class JungleBasinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new Forest());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        Permanent jungleBasin = findPermanent(player1, "Jungle Basin");
        assertThat(jungleBasin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Forest")
    void autoSacrificesWithoutUntappedForest() {
        harness.addToBattlefield(player1, new Forest());
        findPermanent(player1, "Forest").tap();
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Jungle Basin");
        harness.assertInGraveyard(player1, "Jungle Basin");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Accepting returns an untapped Forest and keeps Jungle Basin")
    void acceptReturnsForestAndKeepsJungleBasin() {
        harness.addToBattlefield(player1, new Forest());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Jungle Basin");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Accepting with two untapped Forests lets controller choose which to return")
    void acceptWithTwoForestsChoosesOne() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID forestId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(forestId));

        harness.assertOnBattlefield(player1, "Jungle Basin");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Forest")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not use an untapped Forest controlled by an opponent")
    void ignoresOpponentsUntappedForest() {
        harness.addToBattlefield(player2, new Forest());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Jungle Basin");
        harness.assertInGraveyard(player1, "Jungle Basin");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Returns a controlled Forest to its owner's hand")
    void returnsControlledForestToOwnersHand() {
        Forest forest = new Forest();
        forest.setOwnerId(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(forest));

        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Jungle Basin");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Declining sacrifices Jungle Basin and keeps the Forest")
    void declineSacrificesJungleBasin() {
        harness.addToBattlefield(player1, new Forest());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Jungle Basin");
        harness.assertInGraveyard(player1, "Jungle Basin");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {G}")
    void manaAbilityAddsColorlessAndGreen() {
        harness.addToBattlefield(player1, new JungleBasin());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new JungleBasin()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
