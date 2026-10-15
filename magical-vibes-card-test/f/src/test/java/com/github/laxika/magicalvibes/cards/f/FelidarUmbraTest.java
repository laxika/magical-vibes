package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FelidarUmbra.class, GrizzlyBears.class})
class FelidarUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gains lifelink")
    void grantsLifelinkToEnchantedCreature() {
        Permanent creature = readyCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = readyCreature(player1);
        Permanent aura = attachAura(player1, creature);
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Felidar Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("{1}{W} moves the Aura onto another creature you control")
    void activatedAbilityMovesAura() {
        Permanent first = readyCreature(player1);
        Permanent second = readyCreature(player1);
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareAbilityActivation();

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The reattachment ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent ownCreature = readyCreature(player1);
        Permanent opponentCreature = readyCreature(player2);
        Permanent aura = attachAura(player1, ownCreature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, aura), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void canBeCastOnAnOpponentsCreature() {
        Permanent creature = readyCreature(player2);
        harness.setHand(player1, List.of(new FelidarUmbra()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareAbilityActivation();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Felidar Umbra");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void lifelinkBenefitsCreatureControllerInsteadOfAuraController() {
        Permanent creature = readyCreature(player2);
        attachAura(player1, creature);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        creature.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void umbraArmorDoesNotSaveCreatureWithZeroToughness() {
        Permanent creature = readyCreature(player1);
        attachAura(player1, creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Felidar Umbra");
    }

    @Test
    void creatureControllerChoosesBetweenMultipleUmbraAuras() {
        Permanent creature = readyCreature(player1);
        Permanent first = attachAura(player1, creature);
        Permanent second = attachAura(player1, creature);
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second, creature);
    }

    @Test
    void umbraArmorAndRegenerationRequireCreatureControllersChoice() {
        Permanent creature = readyCreature(player1);
        Permanent aura = attachAura(player1, creature);
        creature.setRegenerationShield(1);
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura, creature);
    }

    @Test
    void reattachmentFailsIfTargetChangesControllerBeforeResolution() {
        Permanent first = readyCreature(player1);
        Permanent second = readyCreature(player1);
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareAbilityActivation();
        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerBattlefields.get(player2.getId()).add(second);

        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }

    private Permanent readyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FelidarUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
