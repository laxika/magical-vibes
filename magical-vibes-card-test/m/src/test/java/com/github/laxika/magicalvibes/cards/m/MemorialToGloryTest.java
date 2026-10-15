package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialToGlory.class})
class MemorialToGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Memorial to Glory enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToGlory()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent memorial = findPermanent(player1, "Memorial to Glory");
        assertThat(memorial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Memorial to Glory produces white mana")
    void tappingProducesWhiteMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack")
    void sacrificeAbilityPutsOnStack() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to Glory");
    }

    @Test
    @DisplayName("Memorial is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Memorial to Glory");
        harness.assertInGraveyard(player1, "Memorial to Glory");
    }

    @Test
    @DisplayName("Resolving sacrifice ability creates two 1/1 white Soldier tokens")
    void resolvingSacrificeAbilityCreatesTwoSoldierTokens() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> soldiers = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .toList();
        assertThat(soldiers).hasSize(2);
        for (Permanent soldier : soldiers) {
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot sacrifice Memorial without the required white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to Glory");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot sacrifice Memorial with insufficient total mana")
    void cannotActivateWithInsufficientMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to Glory");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Memorial can create tokens during its opponent's turn even when newly controlled")
    void canActivateDuringOpponentsTurnWhenNewlyControlled() {
        harness.addToBattlefield(player1, new MemorialToGlory());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(soldier -> {
                    assertThat(soldier.getCard().isToken()).isTrue();
                    assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
                    assertThat(soldier.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
    private Permanent addMemorialReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MemorialToGlory());
        perm.setSummoningSick(false);
        return perm;
    }
}
