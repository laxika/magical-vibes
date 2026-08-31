package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideEntrancer.class, GrizzlyBears.class, Forest.class})
class RiptideEntrancerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may sacrifices Riptide Entrancer and permanently gains control of the damaged player's creature")
    void acceptSacrificeAndGainControl() {
        Permanent attacker = addAttacker();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombatUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertNotOnBattlefield(player1, "Riptide Entrancer");
        harness.assertInGraveyard(player1, "Riptide Entrancer");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.newestControlEffectFor(target.getId()).duration())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The target choice offers only creatures controlled by the damaged player")
    void targetChoiceIsRestrictedToDamagedPlayerCreatures() {
        addAttacker();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = addPermanent(player2, new Forest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        resolveCombatUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId())
                .doesNotContain(land.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("Declining the may keeps both permanents under their original control")
    void declineKeepsBothPermanents() {
        addAttacker();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombatUnblocked();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Riptide Entrancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new RiptideEntrancer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatUnblocked() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
