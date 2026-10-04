package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FontOfFortunes;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.cards.p.PinToTheEarth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GodhunterOctopus.class, FontOfFortunes.class, PensiveMinotaur.class, PinToTheEarth.class})
class GodhunterOctopusTest extends BaseCardTest {

    @Test
    @DisplayName("Godhunter Octopus cannot attack when defending player controls neither condition")
    void cannotAttackWithoutEnchantmentOrEnchantedPermanent() {
        addGodhunterOctopus();

        beginAttackersDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Godhunter Octopus can attack when defending player controls an enchantment")
    void canAttackWhenDefenderControlsEnchantment() {
        harness.addToBattlefield(player2, new FontOfFortunes());
        addGodhunterOctopus();

        beginAttackersDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Godhunter Octopus can attack when defending player controls an enchanted permanent")
    void canAttackWhenDefenderControlsEnchantedPermanent() {
        Permanent defenderCreature = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        defenderCreature.setSummoningSick(false);
        defenderCreature.tap();

        addGodhunterOctopus();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PinToTheEarth());
        aura.setAttachedTo(defenderCreature.getId());

        beginAttackersDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("An enchantment controlled only by the attacker does not permit attacking")
    void cannotAttackWithOnlyControllersEnchantment() {
        addGodhunterOctopus();
        harness.addToBattlefield(player1, new FontOfFortunes());

        beginAttackersDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An enchanted permanent controlled only by the attacker does not permit attacking")
    void cannotAttackWithOnlyControllersEnchantedPermanent() {
        addGodhunterOctopus();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PensiveMinotaur());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PinToTheEarth());
        aura.setAttachedTo(creature.getId());

        beginAttackersDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A defender's Aura permits attacking even when it enchants the attacker's creature")
    void canAttackWhenDefendersAuraEnchantsControllersCreature() {
        addGodhunterOctopus();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PensiveMinotaur());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PinToTheEarth());
        aura.setAttachedTo(creature.getId());

        beginAttackersDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Removing the defender's last enchantment before declaration prevents attacking")
    void cannotAttackAfterDefendersLastEnchantmentLeaves() {
        addGodhunterOctopus();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FontOfFortunes());
        gd.playerBattlefields.get(player2.getId()).remove(enchantment);

        beginAttackersDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addGodhunterOctopus() {
        Permanent octopus = harness.addToBattlefieldAndReturn(player1, new GodhunterOctopus());
        octopus.setSummoningSick(false);
    }

    private void beginAttackersDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
