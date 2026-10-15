package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.e.EagerGlyphmage;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TabletOfDiscovery.class, Shock.class, Mountain.class, LavaAxe.class, EagerGlyphmage.class})
class TabletOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills a card and grants permission to play it this turn")
    void entersMillsAndGrantsPlayPermission() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setHand(player1, List.of(new TabletOfDiscovery()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.graveyardPlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("The first ability adds one red mana")
    void firstAbilityAddsOneRedMana() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Tablet of Discovery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability adds two red mana restricted to instant and sorcery spells")
    void secondAbilityAddsRestrictedRedMana() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The restricted ability cannot cast a creature spell")
    void secondAbilityCannotCastCreature() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        harness.setHand(player1, List.of(new EagerGlyphmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void milledInstantCanBeCastWithItsNormalCost() {
        harness.setLibrary(player1, List.of(new Shock(), new Mountain()));
        harness.enterBattlefieldAndReturn(player1, new TabletOfDiscovery());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void milledLandCanBePlayedButDoesNotGrantAnExtraLandPlay() {
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.enterBattlefieldAndReturn(player1, new TabletOfDiscovery());
        harness.passBothPriorities();
        harness.playLandFromGraveyard(player1, 0);
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Mountain");

        harness.setHand(player1, List.of(new Mountain()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyLibraryDoesNotGrantPermissionToAnExistingGraveyardCard() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.enterBattlefieldAndReturn(player1, new TabletOfDiscovery());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.assertOnBattlefield(player1, "Tablet of Discovery");
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardPermissionExpiresAfterTheTurn() {
        harness.setLibrary(player1, List.of(new Shock(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.enterBattlefieldAndReturn(player1, new TabletOfDiscovery());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCanPayForASorcery() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    void restrictedManaCannotPayForAnArtifact() {
        harness.addToBattlefield(player1, new TabletOfDiscovery());
        harness.setHand(player1, List.of(new TabletOfDiscovery()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Tablet of Discovery");
    }
}
