package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynodSanctum.class, SeatOfTheSynod.class, Arrest.class, SilverMyr.class})
class SynodSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T}: Exile target permanent you control, tracked with Synod Sanctum")
    void exileAbilityExilesOwnPermanent() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        Permanent seat = harness.addToBattlefieldAndReturn(player1, new SeatOfTheSynod());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, seat.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seat of the Synod");
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId()))
                .anyMatch(c -> c.getName().equals("Seat of the Synod"));
    }

    @Test
    @DisplayName("{2}, {T} cannot target a permanent you don't control")
    void exileAbilityCannotTargetOpponentPermanent() {
        harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        Permanent enemySeat = harness.addToBattlefieldAndReturn(player2, new SeatOfTheSynod());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemySeat.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{2}, Sacrifice: return cards exiled with Synod Sanctum under your control")
    void sacrificeReturnsExiledPermanents() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        Permanent seat = harness.addToBattlefieldAndReturn(player1, new SeatOfTheSynod());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, seat.getId());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Sanctum");
        harness.assertInGraveyard(player1, "Synod Sanctum");
        harness.assertOnBattlefield(player1, "Seat of the Synod");
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).isEmpty();
    }

    @Test
    @DisplayName("{2}, Sacrifice: return all permanents exiled with Synod Sanctum")
    void sacrificeReturnsAllExiledPermanents() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        List<Permanent> seats = findPermanents(player1, "Seat of the Synod");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, seats.get(0).getId());
        harness.passBothPriorities();
        sanctum.untap();
        harness.activateAbility(player1, 0, 0, null, seats.get(1).getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).hasSize(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Seat of the Synod")).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).isEmpty();
    }

    @Test
    @DisplayName("Return ability puts an exiled permanent under its controller's control")
    void sacrificeReturnsPermanentUnderControllerControl() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        SeatOfTheSynod seatCard = new SeatOfTheSynod();
        seatCard.setOwnerId(player2.getId());
        Permanent seat = harness.addToBattlefieldAndReturn(player1, seatCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, seat.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seat of the Synod");
        harness.assertNotOnBattlefield(player2, "Seat of the Synod");
        assertThat(gd.getCardsExiledByPermanent(sanctum.getId())).isEmpty();
    }

    @Test
    @DisplayName("{2}, Sacrifice with no exiled permanents still sacrifices Synod Sanctum")
    void sacrificeWithNoExiledPermanentsStillSacrificesSanctum() {
        harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Sanctum");
        harness.assertInGraveyard(player1, "Synod Sanctum");
    }

    @Test
    void sacrificeInResponseDoesNotReturnThePendingExileTarget() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        Permanent seat = harness.addToBattlefieldAndReturn(player1, new SeatOfTheSynod());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, seat.getId());
        assertThat(sanctum.isTapped()).isTrue();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Synod Sanctum");
        harness.assertNotOnBattlefield(player1, "Synod Sanctum");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Seat of the Synod");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seat of the Synod");
        assertThat(gd.findExiledCard(seat.getCard().getId())).isNotNull();
    }

    @Test
    void aDifferentSanctumCannotReturnCardsExiledByTheFirst() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addToBattlefield(player1, new SynodSanctum());
        Permanent seat = harness.addToBattlefieldAndReturn(player1, new SeatOfTheSynod());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, seat.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seat of the Synod");
        assertThat(gd.getCardsExiledByPermanent(first.getId())).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Seat of the Synod");
    }

    @Test
    void canExileItself() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new SynodSanctum());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, sanctum.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Sanctum");
        harness.assertNotInGraveyard(player1, "Synod Sanctum");
        assertThat(gd.findExiledCard(sanctum.getCard().getId())).isNotNull();
    }

    @Test
    void returningAnAuraAllowsChoosingWhatItEnchants() {
        harness.addToBattlefield(player1, new SynodSanctum());
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new SilverMyr());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, myr.getId());
        harness.passBothPriorities();
        Permanent arrest = findPermanent(player1, "Arrest");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, arrest.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, myr.getId());
        }

        harness.assertOnBattlefield(player1, "Arrest");
        assertThat(findPermanent(player1, "Arrest").getAttachedTo()).isEqualTo(myr.getId());
    }

    @Test
    void anAuraWithNothingLegalToEnchantRemainsInExile() {
        harness.addToBattlefield(player1, new SynodSanctum());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new SilverMyr());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, myr.getId());
        harness.passBothPriorities();
        Permanent arrest = findPermanent(player1, "Arrest");
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, arrest.getId());
        harness.passBothPriorities();
        findPermanent(player1, "Synod Sanctum").untap();
        harness.activateAbility(player1, 0, 0, null, myr.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silver Myr");
        harness.assertNotOnBattlefield(player1, "Arrest");
        harness.assertNotInGraveyard(player1, "Arrest");
        assertThat(gd.findExiledCard(arrest.getCard().getId())).isNotNull();
    }
}
