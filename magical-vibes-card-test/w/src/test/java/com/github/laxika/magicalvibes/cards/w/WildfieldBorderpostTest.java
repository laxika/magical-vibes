package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArcaneSanctum;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({WildfieldBorderpost.class, Plains.class, ArcaneSanctum.class})
class WildfieldBorderpostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped when cast for its full mana cost")
    void entersTappedOnNormalCast() {
        harness.setHand(player1, List.of(new WildfieldBorderpost()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent borderpost = borderpost(player1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast by paying {1} and returning a basic land to its owner's hand")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        UUID plains = harness.getPermanentId(player1, "Plains");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(plains));
        harness.passBothPriorities();

        // Borderpost is on the battlefield, entered tapped.
        assertThat(borderpost(player1).isTapped()).isTrue();
        // The basic land is returned to its owner's hand, not sacrificed.
        harness.assertInHand(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        // Only {1} was paid.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Alternate cost is rejected when the returned land is not basic")
    void alternateCostRejectsNonBasicLand() {
        harness.addToBattlefield(player1, new ArcaneSanctum());
        UUID sanctum = harness.getPermanentId(player1, "Arcane Sanctum");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(sanctum)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails without the {1} mana payment")
    void alternateCostRequiresMana() {
        harness.addToBattlefield(player1, new Plains());
        UUID plains = harness.getPermanentId(player1, "Plains");
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(plains)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("{T}: Add {G} or {W} — choosing green adds one green mana and taps it")
    void manaAbilityAddsGreen() {
        Permanent borderpost = addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}: Add {G} or {W} — choosing white adds one white mana")
    void manaAbilityAddsWhite() {
        addReadyBorderpost(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped basic land can be returned and is paid before the spell resolves")
    void alternateCostCanReturnTappedLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        plains.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(plains.getId()));

        harness.assertInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Wildfield Borderpost");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The alternate cost cannot return an opponent's basic land")
    void alternateCostRejectsOpponentsLand() {
        harness.addToBattlefield(player1, new Plains());
        Permanent opponentsPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(opponentsPlains.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInHand(player1, "Wildfield Borderpost");
    }

    @Test
    @DisplayName("A controlled basic land owned by the opponent returns to that opponent's hand")
    void alternateCostReturnsLandToItsOwner() {
        Plains plainsCard = new Plains();
        plainsCard.setOwnerId(player2.getId());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, plainsCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new WildfieldBorderpost()));

        harness.castWithAlternateCost(player1, 0, List.of(plains.getId()));

        harness.assertInHand(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");

        harness.passBothPriorities();

        assertThat(borderpost(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly controlled noncreature Borderpost can activate its mana ability")
    void manaAbilityDoesNotRequireClearingSummoningSickness() {
        Permanent borderpost = harness.addToBattlefieldAndReturn(player1, new WildfieldBorderpost());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(borderpost.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent borderpost(Player player) {
        return findPermanent(player, "Wildfield Borderpost");
    }

    private Permanent addReadyBorderpost(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WildfieldBorderpost());
        perm.setSummoningSick(false);
        return perm;
    }
}
