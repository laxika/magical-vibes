package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.z.ZephyrNet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player2, new GloriousAnthem());
        addGodhunterOctopus();

        beginAttackersDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Godhunter Octopus can attack when defending player controls an enchanted permanent")
    void canAttackWhenDefenderControlsEnchantedPermanent() {
        Permanent defenderCreature = new Permanent(new GrizzlyBears());
        defenderCreature.setSummoningSick(false);
        defenderCreature.tap();
        gd.playerBattlefields.get(player2.getId()).add(defenderCreature);

        addGodhunterOctopus();

        Permanent aura = new Permanent(new ZephyrNet());
        aura.setAttachedTo(defenderCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        beginAttackersDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    private Permanent addGodhunterOctopus() {
        Permanent octopus = new Permanent(new GodhunterOctopus());
        octopus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(octopus);
        return octopus;
    }

    private void beginAttackersDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
