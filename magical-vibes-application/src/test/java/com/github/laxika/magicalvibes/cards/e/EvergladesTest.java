package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({Everglades.class, Plains.class, Swamp.class})
class EvergladesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new Swamp());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        Permanent everglades = findPermanent(player1, "Everglades");
        assertThat(everglades.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Swamp")
    void autoSacrificesWithoutUntappedSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Everglades");
        harness.assertInGraveyard(player1, "Everglades");
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Does not use an untapped Swamp controlled by an opponent")
    void ignoresOpponentsUntappedSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Everglades");
        harness.assertInGraveyard(player1, "Everglades");
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("Accepting returns an untapped Swamp and keeps Everglades")
    void acceptReturnsSwampAndKeepsEverglades() {
        harness.addToBattlefield(player1, new Swamp());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Everglades");
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Accepting with two untapped Swamps lets controller choose which to return")
    void acceptWithTwoSwampsChoosesOne() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID swampId = findPermanent(player1, "Swamp").getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(swampId));

        harness.assertOnBattlefield(player1, "Everglades");
        assertThat(countPermanents(player1, "Swamp")).isEqualTo(1);
        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Declining sacrifices Everglades and keeps the Swamp")
    void declineSacrificesEverglades() {
        harness.addToBattlefield(player1, new Swamp());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Everglades");
        harness.assertInGraveyard(player1, "Everglades");
        harness.assertOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {B}")
    void manaAbilityAddsColorlessAndBlack() {
        harness.addToBattlefield(player1, new Everglades());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Everglades");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new Everglades()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
