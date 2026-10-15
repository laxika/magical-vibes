package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SpireGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryOfDespair.class, DarksteelIngot.class, DrossGolem.class, DroolingOgre.class,
        SpireGolem.class})
class EmissaryOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player loses life for each artifact they control")
    void losesLifePerArtifactControlledByDamagedPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player1, new DarksteelIngot());
        addCreatureReady(player2, new DarksteelIngot());
        addCreatureReady(player2, new DrossGolem());
        addCreatureReady(player2, new DroolingOgre());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Causes no extra life loss when the damaged player controls no artifacts")
    void noExtraLifeLossWithoutArtifacts() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player2, new DroolingOgre());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player2, new DarksteelIngot());
        Permanent blocker = addCreatureReady(player2, new SpireGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts artifacts that enter after combat damage but before resolution")
    void countsArtifactsAtResolution() {
        harness.setLife(player2, 20);
        dealCombatDamageWithoutResolvingTrigger();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.addToBattlefield(player2, new DrossGolem());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count artifacts that leave before the trigger resolves")
    void ignoresArtifactsThatLeftBeforeResolution() {
        harness.setLife(player2, 20);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DrossGolem());
        dealCombatDamageWithoutResolvingTrigger();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The trigger still resolves after Emissary leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new DarksteelIngot());
        Permanent emissary = dealCombatDamageWithoutResolvingTrigger();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(emissary);
        gd.playerGraveyards.get(player1.getId()).add(emissary.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    private Permanent dealCombatDamageWithoutResolvingTrigger() {
        Permanent emissary = addCreatureReady(player1, new EmissaryOfDespair());
        emissary.setAttacking(true);
        emissary.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        return emissary;
    }

    private void resolveCombatAndTrigger() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
    }
}
