package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ConversionChamber.class, PorcelainLegionnaire.class, GlistenerElf.class})
class ConversionChamberTest extends BaseCardTest {

    @Test
    @DisplayName("A newly entered noncreature Chamber can use its tap ability")
    void newlyEnteredChamberCanActivate() {
        Permanent chamber = harness.enterBattlefieldAndReturn(player1, new ConversionChamber());
        Card artifact = new PorcelainLegionnaire();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(chamber.isTapped()).isTrue();
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Porcelain Legionnaire");
    }

    

    

    // ===== First ability — exile artifact from graveyard and gain charge counter =====

    @Test
    @DisplayName("Activating first ability exiles artifact from controller's graveyard and adds charge counter")
    void firstAbilityExilesArtifactAndAddsCounter() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod removed from graveyard
        harness.assertNotInGraveyard(player1, "Porcelain Legionnaire");

        // Rod is in player's exiled cards
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Porcelain Legionnaire"));

        // Charge counter added
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability can exile artifact from opponent's graveyard")
    void firstAbilityExilesFromOpponentGraveyard() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Rod removed from opponent's graveyard
        harness.assertNotInGraveyard(player2, "Porcelain Legionnaire");

        // Rod is in opponent's exiled cards (cards owned by graveyard owner)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Porcelain Legionnaire"));

        // Charge counter still added to chamber
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability does NOT imprint on source permanent")
    void firstAbilityDoesNotImprint() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Should NOT be tracked in permanentExiledCards
        assertThat(gd.getCardsExiledByPermanent(chamber.getId())).isEmpty();
    }

    @Test
    @DisplayName("First ability rejects non-artifact card as target")
    void firstAbilityRejectsNonArtifact() {
        Permanent chamber = addChamberReady(player1);
        Card bears = new GlistenerElf();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability rejects target not in any graveyard")
    void firstAbilityRejectsTargetNotInGraveyard() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("First ability fizzles if target removed from graveyard before resolution")
    void firstAbilityFizzlesIfTargetRemoved() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);

        // Remove target before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No charge counter added since exile fizzled
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("First ability taps the artifact")
    void firstAbilityTaps() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);

        assertThat(chamber.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability without enough mana")
    void cannotActivateFirstAbilityWithoutMana() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        Permanent chamber = addChamberReady(player1);
        Card rod1 = new PorcelainLegionnaire();
        Card rod2 = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod1, rod2)));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        // First activation
        harness.activateAbility(player1, chamberIndex, 0, null, rod1.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Untap for second activation
        chamber.untap();
        harness.activateAbility(player1, chamberIndex, 0, null, rod2.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    // ===== Second ability — token creation =====

    @Test
    @DisplayName("Activating second ability with 1 charge counter creates a 3/3 Golem artifact creature token")
    void secondAbilityCreatesGolemToken() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // 3/3 Golem artifact creature token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Cannot activate second ability without charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent chamber = addChamberReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability without enough mana")
    void cannotActivateSecondAbilityWithoutMana() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability taps the artifact")
    void secondAbilityTaps() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);

        assertThat(chamber.isTapped()).isTrue();
    }

    @Test
    @DisplayName("With multiple charge counters, second ability only removes 1")
    void secondAbilityRemovesExactlyOneCounter() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    // ===== Both abilities interaction =====

    @Test
    @DisplayName("Cannot activate either ability when tapped")
    void cannotActivateWhenTapped() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        chamber.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, chamberIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Full flow: exile artifact, then create token")
    void fullFlowExileThenCreateToken() {
        Permanent chamber = addChamberReady(player1);
        Card rod = new PorcelainLegionnaire();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int chamberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(chamber);

        // Step 1: Exile artifact from graveyard
        harness.activateAbility(player1, chamberIndex, 0, null, rod.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Step 2: Untap and create token
        chamber.untap();
        harness.activateAbility(player1, chamberIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // Golem token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("The charge counter is paid before the token ability resolves")
    void counterIsPaidAtActivation() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertNotOnBattlefield(player1, "Golem");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Golem");
                    assertThat(token.getCard().getPower()).isEqualTo(3);
                    assertThat(token.getCard().getToughness()).isEqualTo(3);
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
                    assertThat(token.getCard().getColors()).isEmpty();
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
                });
        harness.assertNotOnBattlefield(player2, "Golem");
    }

    @Test
    @DisplayName("The exile ability requires a target even when there are no artifacts in graveyards")
    void firstAbilityRequiresTarget() {
        Permanent chamber = addChamberReady(player1);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(chamber.isTapped()).isFalse();
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Exiling the target still happens when the Chamber leaves before resolution")
    void exileResolvesWithoutSource() {
        Permanent chamber = addChamberReady(player1);
        Card artifact = new PorcelainLegionnaire();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chamber));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Porcelain Legionnaire");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact);
        harness.assertInGraveyard(player1, "Conversion Chamber");
        assertThat(chamber.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The token ability resolves after the Chamber leaves the battlefield")
    void tokenResolvesWithoutSource() {
        Permanent chamber = addChamberReady(player1);
        chamber.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chamber));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conversion Chamber");
        harness.assertOnBattlefield(player1, "Golem");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent addChamberReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ConversionChamber());
        perm.setSummoningSick(false);
        return perm;
    }
}
