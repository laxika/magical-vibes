package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArcaneSanctum;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeinfireBorderpost.class, Mountain.class, ArcaneSanctum.class})
class VeinfireBorderpostTest extends BaseCardTest {


    @Test
    @DisplayName("Enters the battlefield tapped when cast for its full mana cost")
    void entersTappedOnNormalCast() {
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent borderpost = borderpost(player1);
        assertThat(borderpost.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Can be cast by paying {1} and returning a basic land to its owner's hand")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new Mountain());
        UUID mountain = harness.getPermanentId(player1, "Mountain");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new VeinfireBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain));
        harness.passBothPriorities();

        // Borderpost is on the battlefield, entered tapped.
        assertThat(borderpost(player1).isTapped()).isTrue();
        // The basic land is returned to its owner's hand, not sacrificed.
        harness.assertInHand(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");
        // Only {1} was paid.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Alternate cost is rejected when the returned land is not basic")
    void alternateCostRejectsNonBasicLand() {
        harness.addToBattlefield(player1, new ArcaneSanctum());
        UUID sanctum = harness.getPermanentId(player1, "Arcane Sanctum");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new VeinfireBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(sanctum)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails without the {1} mana payment")
    void alternateCostRequiresMana() {
        harness.addToBattlefield(player1, new Mountain());
        UUID mountain = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new VeinfireBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }


    @Test
    @DisplayName("{T}: Add {B} or {R} — choosing black adds one black mana and taps it")
    void manaAbilityAddsBlack() {
        Permanent borderpost = addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}: Add {B} or {R} — choosing red adds one red mana")
    void manaAbilityAddsRed() {
        addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }


    @Test
    @DisplayName("A tapped basic land can be returned and is returned before resolution")
    void returnsTappedLandAsCastingCost() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Veinfire Borderpost");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's basic land cannot pay the alternate cost")
    void cannotReturnOpponentsLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Veinfire Borderpost");
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An untapped noncreature Borderpost can produce mana the turn it enters")
    void manaAbilityDoesNotRequireHaste() {
        Permanent borderpost = harness.addToBattlefieldAndReturn(player1, new VeinfireBorderpost());
        borderpost.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(borderpost.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("A controlled basic land owned by an opponent returns to its owner's hand")
    void returnsLandToItsOwner() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        gd.stolenCreatures.put(mountain.getId(), player2.getId());
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player2, "Mountain");
        harness.assertNotInHand(player1, "Mountain");
        harness.passBothPriorities();
        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The alternate cost requires exactly one returned basic land")
    void requiresOneReturnedLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new VeinfireBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Veinfire Borderpost");
    }
    private Permanent borderpost(Player player) {
        return findPermanent(player, "Veinfire Borderpost");
    }

    private Permanent addReadyBorderpost(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VeinfireBorderpost());
        perm.setSummoningSick(false);
        return perm;
    }
}
