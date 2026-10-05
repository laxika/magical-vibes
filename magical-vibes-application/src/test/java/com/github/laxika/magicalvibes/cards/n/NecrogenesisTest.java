package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necrogenesis.class, CylianElf.class, Cancel.class})
class NecrogenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creature card from controller's graveyard and creates 1/1 green Saproling token")
    void exilesCreatureAndCreatesToken() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card bears = new CylianElf();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Creature card exiled from graveyard
        harness.assertNotInGraveyard(player1, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cylian Elf"));

        // 1/1 green Saproling token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Saproling")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.SAPROLING));
    }

    @Test
    @DisplayName("Can exile creature card from opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card bears = new CylianElf();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cylian Elf"));

        // Token created under controller's control
        harness.assertOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Rejects non-creature card as target")
    void rejectsNonCreatureTarget() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card cancel = new Cancel();
        harness.setGraveyard(player1, List.of(cancel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, cancel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card bears = new CylianElf();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card bears = new CylianElf();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId(), Zone.GRAVEYARD);

        // Remove target before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No token created since exile fizzled
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Cannot activate without a creature card target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new Necrogenesis());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Two activations targeting the same card create only one token")
    void competingActivationsCreateOnlyOneToken() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card creature = new CylianElf();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Saproling"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Saproling");
    }

    @Test
    @DisplayName("Ability resolves even if Necrogenesis leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new Necrogenesis());
        Card creature = new CylianElf();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player1, "Saproling");
        harness.assertNotOnBattlefield(player2, "Saproling");
    }
}
