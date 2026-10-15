package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialToWar.class, Mountain.class, BalothGorger.class})
class MemorialToWarTest extends BaseCardTest {

    @Test
    @DisplayName("Memorial to War enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToWar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent memorial = findPermanent(player1, "Memorial to War");
        assertThat(memorial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Memorial to War produces red mana")
    void tappingProducesRedMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        harness.tapPermanent(player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack")
    void sacrificeAbilityPutsOnStack() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to War");
    }

    @Test
    @DisplayName("Memorial is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        harness.assertNotOnBattlefield(player1, "Memorial to War");
        harness.assertInGraveyard(player1, "Memorial to War");
    }

    @Test
    @DisplayName("Resolving sacrifice ability destroys target land")
    void resolvingSacrificeAbilityDestroysTargetLand() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a creature with sacrifice ability")
    void cannotTargetCreature() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Baloth Gorger");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target land is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.activateAbility(player1, 0, null, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability can destroy a land you control")
    void canDestroyOwnLand() {
        addMemorialReady(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Mountain"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Memorial to War");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Memorial can target itself and is sacrificed before the ability resolves")
    void canTargetItself() {
        Permanent memorial = addMemorialReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, memorial.getId());

        harness.assertNotOnBattlefield(player1, "Memorial to War");
        harness.assertInGraveyard(player1, "Memorial to War");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifice ability requires red mana and leaves costs unpaid on rejection")
    void cannotActivateWithoutRedMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to War");
        harness.assertNotInGraveyard(player1, "Memorial to War");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Memorial can activate its tap ability on the turn it enters")
    void canActivateDespiteSummoningSicknessFlag() {
        Permanent memorial = harness.addToBattlefieldAndReturn(player1, new MemorialToWar());
        memorial.setSummoningSick(true);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Mountain"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Memorial to War");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated with too little generic mana")
    void cannotActivateWithInsufficientMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to War");
        harness.assertNotInGraveyard(player1, "Memorial to War");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMemorialReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MemorialToWar());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
