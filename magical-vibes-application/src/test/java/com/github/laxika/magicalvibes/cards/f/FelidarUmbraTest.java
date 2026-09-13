package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private Permanent readyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new FelidarUmbra());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
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
