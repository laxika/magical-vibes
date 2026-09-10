package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsCurse.class, BottleGnomes.class})
class VolrathsCurseTest extends BaseCardTest {

    /** Adds ready Bottle Gnomes so an opponent-controlled Aura is at a predictable index. */
    private void addFillers(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new BottleGnomes());
        }
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        addCreatureReady(player2, new BottleGnomes());
        Permanent blocker = addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(blocker.getId());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("The enchanted creature's controller sacrifices a permanent to unlock its abilities this turn")
    void sacrificingAPermanentIgnoresTheCurse() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent sacrificeTarget = addCreatureReady(player1, new BottleGnomes());

        addFillers(player2, 2);
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == sacrificeTarget.getCard());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("The ignore effect wears off at end of turn")
    void ignoreEffectWearsOffAtEndOfTurn() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent sacrificeTarget = addCreatureReady(player1, new BottleGnomes());

        addFillers(player2, 2);
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();
        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Ignoring the Curse lets the enchanted creature attack")
    void ignoringTheCurseLetsTheCreatureAttack() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent sacrificeTarget = addCreatureReady(player1, new BottleGnomes());

        addFillers(player2, 2);
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        assertThat(gnomes.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a permanent to ignore the Curse takes effect without using the stack")
    void sacrificingAPermanentToIgnoreTheCurseIsImmediate() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent sacrificeTarget = addCreatureReady(player1, new BottleGnomes());

        addFillers(player2, 2);
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(curse.isAuraEffectsIgnoredThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The Aura's controller may not activate the sacrifice ability")
    void auraControllerCannotActivateSacrificeAbility() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());
        addCreatureReady(player2, new BottleGnomes());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
    }

    @Test
    @DisplayName("The sacrifice permission can be used only once each turn")
    void sacrificePermissionCanBeUsedOnlyOnceEachTurn() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent firstSacrificeTarget = addCreatureReady(player1, new BottleGnomes());

        addFillers(player2, 2);
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, firstSacrificeTarget.getId());
        harness.passBothPriorities();

        addCreatureReady(player1, new BottleGnomes());
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The enchanted creature's controller may sacrifice the Aura itself")
    void enchantedCreatureControllerMaySacrificeTheAuraItself() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());
        addCreatureReady(player1, new BottleGnomes());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.handlePermanentChosen(player1, curse.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent == curse);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == curse.getCard());
    }

    @Test
    @DisplayName("{1}{U} returns the Aura to its owner's hand, freeing the creature")
    void bounceAbilityReturnsAuraToHand() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new VolrathsCurse());
        curse.setAttachedTo(gnomes.getId());

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card == curse.getCard());

        declareAttackers(player1, List.of(0));
    }
}
