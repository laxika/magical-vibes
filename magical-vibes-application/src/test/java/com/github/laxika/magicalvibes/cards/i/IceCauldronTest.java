package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IceCauldronTest extends BaseCardTest {

    /**
     * Activates the first ability for {X} = 2, paying one green and one colorless mana, and exiles
     * Grizzly Bears from hand. Leaves the game just after the exile choice was answered.
     */
    private Card exileBearsForOneGreenOneColorless() {
        harness.addToBattlefield(player1, new IceCauldron());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        return harness.getGameData().getPlayerExiledCards(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("First ability exiles a nonland card from hand and lets its controller cast it")
    void firstAbilityExilesWithCastPermission() {
        Card bears = exileBearsForOneGreenOneColorless();
        GameData gd = harness.getGameData();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("First ability puts a charge counter on Ice Cauldron")
    void firstAbilityAddsChargeCounter() {
        exileBearsForOneGreenOneColorless();

        Permanent cauldron = findPermanent(player1, "Ice Cauldron");
        assertThat(cauldron.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability can't be activated while a charge counter is on Ice Cauldron")
    void firstAbilityBlockedByChargeCounter() {
        exileBearsForOneGreenOneColorless();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability removes the charge counter and adds the noted mana for the exiled card")
    void secondAbilityAddsNotedMana() {
        Card bears = exileBearsForOneGreenOneColorless();
        findPermanent(player1, "Ice Cauldron").untap();

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        Permanent cauldron = findPermanent(player1, "Ice Cauldron");
        assertThat(cauldron.getCounterCount(CounterType.CHARGE)).isZero();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotal()).isZero();
        assertThat(pool.getExiledCardOnlyMana(bears.getId()))
                .containsEntry(ManaColor.GREEN, 1)
                .containsEntry(ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Noted mana pays for casting the exiled card")
    void notedManaCastsTheExiledCard() {
        Card bears = exileBearsForOneGreenOneColorless();
        findPermanent(player1, "Ice Cauldron").untap();
        harness.activateAbility(player1, 0, 1, null, null);

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getExiledCardOnlyMana(bears.getId())).isEmpty();
    }

    @Test
    @DisplayName("Noted mana can't be spent on anything but the card it was reserved for")
    void notedManaIsReservedForTheExiledCard() {
        exileBearsForOneGreenOneColorless();
        findPermanent(player1, "Ice Cauldron").untap();
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the exile still notes the mana and adds a charge counter")
    void decliningExileStillNotesMana() {
        harness.addToBattlefield(player1, new IceCauldron());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Ice Cauldron").getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        UUID cauldronCardId = findPermanent(player1, "Ice Cauldron").getCard().getId();
        assertThat(gd.notedMana.get(cauldronCardId)).containsEntry(ManaColor.GREEN, 2);
    }
}
