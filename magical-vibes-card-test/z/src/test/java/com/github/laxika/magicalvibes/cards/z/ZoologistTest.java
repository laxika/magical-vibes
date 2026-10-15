package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Zoologist.class, DuskImp.class, Forest.class})
class ZoologistTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature card is put onto the battlefield")
    void creatureCardPutOntoBattlefield() {
        Permanent zoologist = addReadyZoologist();
        Card creature = new DuskImp();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(zoologist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Revealed non-creature card is put into the graveyard")
    void nonCreatureCardPutIntoGraveyard() {
        addReadyZoologist();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryEmpty() {
        Permanent zoologist = addReadyZoologist();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId())
                .isEqualTo(zoologist.getId());
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent zoologist = addReadyZoologist();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(zoologist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without the required green mana")
    void cannotActivateWithoutRequiredGreenMana() {
        Permanent zoologist = addReadyZoologist();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(zoologist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent zoologist = addReadyZoologist();
        zoologist.tap();
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Zoologist());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Only the top card moves and the remaining library order is preserved")
    void onlyTopCardMoves() {
        addReadyZoologist();
        Card top = new Forest();
        Card second = new DuskImp();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An activated ability still resolves after Zoologist leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent zoologist = addReadyZoologist();
        Card creature = new DuskImp();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(zoologist);
        harness.setGraveyard(player1, List.of(zoologist.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(zoologist.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability uses its controller's library and puts the creature under their control")
    void usesAbilityControllersLibrary() {
        addCreatureReady(player2, new Zoologist());
        Card creature = new DuskImp();
        Card otherTop = new Forest();
        harness.setLibrary(player1, List.of(otherTop));
        harness.setLibrary(player2, List.of(creature));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherTop);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        Permanent entered = findPermanent(player2, "Dusk Imp");
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isSummoningSick()).isTrue();
    }
    private Permanent addReadyZoologist() {
        return addCreatureReady(player1, new Zoologist());
    }
}
