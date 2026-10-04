package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FirewildBorderpost.class, Mountain.class, ArcaneSanctum.class})
class FirewildBorderpostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped when cast for its full mana cost")
    void entersTappedOnNormalCast() {
        harness.setHand(player1, List.of(new FirewildBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

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
        harness.setHand(player1, List.of(new FirewildBorderpost()));

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
        harness.setHand(player1, List.of(new FirewildBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(sanctum)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails without the {1} mana payment")
    void alternateCostRequiresMana() {
        harness.addToBattlefield(player1, new Mountain());
        UUID mountain = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new FirewildBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("{T}: Add {R} or {G} — choosing red adds one red mana and taps it")
    void manaAbilityAddsRed() {
        Permanent borderpost = addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}: Add {R} or {G} — choosing green adds one green mana")
    void manaAbilityAddsGreen() {
        addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped basic land can be returned, and the return is paid before resolution")
    void alternateCostReturnsTappedLandImmediately() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FirewildBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Firewild Borderpost");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controlled basic land returns to its owner, even when another player owns it")
    void alternateCostReturnsLandToItsOwner() {
        Mountain mountainCard = new Mountain();
        mountainCard.setOwnerId(player2.getId());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, mountainCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new FirewildBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player2, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's basic land cannot pay the alternate cost")
    void alternateCostRejectsOpponentsLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new FirewildBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found on your battlefield");

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Firewild Borderpost");
    }

    @Test
    @DisplayName("A tapped Borderpost cannot activate its mana ability again")
    void manaAbilityCannotBeActivatedTwiceWithoutUntapping() {
        addReadyBorderpost(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Borderpost can produce mana even with summoning sickness")
    void manaAbilityDoesNotRequireControlSinceTurnStart() {
        Permanent borderpost = harness.addToBattlefieldAndReturn(player1, new FirewildBorderpost());
        borderpost.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(borderpost.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent borderpost(Player player) {
        return findPermanent(player, "Firewild Borderpost");
    }

    private Permanent addReadyBorderpost(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FirewildBorderpost());
        perm.setSummoningSick(false);
        return perm;
    }
}
