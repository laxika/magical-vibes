package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrissSamiteGuardian.class, NessianCourser.class, Ghostfire.class, DakmorSalvage.class})
class OrissSamiteGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability prevents all damage to target creature this turn")
    void preventsAllDamageToTargetCreature() {
        Permanent oriss = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        oriss.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(oriss.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability prevents damage only to the chosen creature")
    void preventsDamageOnlyToChosenCreature() {
        Permanent oriss = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        oriss.setSummoningSick(false);
        Permanent protectedCreature = addCreatureReady(player1, new NessianCourser());
        Permanent unprotectedCreature = addCreatureReady(player1, new NessianCourser());

        harness.activateAbility(player1, 0, null, protectedCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, unprotectedCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature)
                .doesNotContain(unprotectedCreature);
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityRequiresCreatureTarget() {
        Permanent oriss = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        oriss.setSummoningSick(false);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DakmorSalvage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(oriss.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur discards another Oriss and locks the target player")
    void grandeurLocksTargetPlayer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        source.setSummoningSick(false);
        Permanent creature = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new NessianCourser()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, creature));
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, source));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Oriss, Samite Guardian"));
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur cannot be paid with a different card")
    void grandeurRequiresAnotherOriss() {
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        harness.setHand(player1, List.of(new NessianCourser()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
