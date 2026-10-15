package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcaneSanctum;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({MistveinBorderpost.class, Island.class, ArcaneSanctum.class})
class MistveinBorderpostTest extends BaseCardTest {

    // ===== Enters tapped =====

    @Test
    @DisplayName("Enters the battlefield tapped when cast for its full mana cost")
    void entersTappedOnNormalCast() {
        harness.setHand(player1, List.of(new MistveinBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent borderpost = borderpost(player1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    // ===== Alternate casting cost =====

    @Test
    @DisplayName("Can be cast by paying {1} and returning a basic land to its owner's hand")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new Island());
        UUID island = harness.getPermanentId(player1, "Island");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MistveinBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(island));
        harness.passBothPriorities();

        // Borderpost is on the battlefield, entered tapped.
        assertThat(borderpost(player1).isTapped()).isTrue();
        // The basic land is returned to its owner's hand, not sacrificed.
        harness.assertInHand(player1, "Island");
        harness.assertNotInGraveyard(player1, "Island");
        // Only {1} was paid.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Alternate cost is rejected when the returned land is not basic")
    void alternateCostRejectsNonBasicLand() {
        harness.addToBattlefield(player1, new ArcaneSanctum());
        UUID sanctum = harness.getPermanentId(player1, "Arcane Sanctum");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MistveinBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(sanctum)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails without the {1} mana payment")
    void alternateCostRequiresMana() {
        harness.addToBattlefield(player1, new Island());
        UUID island = harness.getPermanentId(player1, "Island");
        harness.setHand(player1, List.of(new MistveinBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(island)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("{T}: Add {U} or {B} — choosing blue adds one blue mana and taps it")
    void manaAbilityAddsBlue() {
        Permanent borderpost = addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}: Add {U} or {B} — choosing black adds one black mana")
    void manaAbilityAddsBlack() {
        addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void alternateCostCanReturnTappedLandBeforeSpellResolves() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new MistveinBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(island.getId()));

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Mistvein Borderpost");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    void alternateCostCannotReturnOpponentsLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MistveinBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Mistvein Borderpost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedBorderpostCannotActivateManaAbility() {
        harness.enterBattlefieldAndReturn(player1, new MistveinBorderpost());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void manaAbilityWorksImmediatelyAfterEnteringIfUntapped() {
        Permanent borderpost = harness.enterBattlefieldAndReturn(player1, new MistveinBorderpost());
        borderpost.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent borderpost(Player player) {
        return findPermanent(player, "Mistvein Borderpost");
    }

    private Permanent addReadyBorderpost(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MistveinBorderpost());
        perm.setSummoningSick(false);
        return perm;
    }
}
