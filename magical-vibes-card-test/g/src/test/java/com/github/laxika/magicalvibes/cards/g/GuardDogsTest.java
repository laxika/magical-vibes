package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.h.HuntingDrake;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.cards.s.SlingshotGoblin;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardDogs.class, AuroraGriffin.class, TerminalMoraine.class, MoggSentry.class,
        SlingshotGoblin.class, HuntingDrake.class})
class GuardDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a permanent on resolution and prevents combat damage from a creature sharing its color")
    void preventsCombatDamageWhenColorsShare() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        Permanent chosen = addCreatureReady(player1, new AuroraGriffin());
        Permanent attacker = addCreatureReady(player2, new AuroraGriffin());

        addAbilityMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null,
                attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, chosen.getId());

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    @DisplayName("Does not prevent combat damage when the chosen permanent and target have no color in common")
    void doesNotPreventCombatDamageWhenColorsDoNotShare() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        Permanent attacker = addCreatureReady(player2, new AuroraGriffin());

        addAbilityMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null,
                attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat - 2);
    }

    @Test
    void cannotTargetANoncreature() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only prevents combat damage, not noncombat damage from the targeted creature")
    void preventsCombatDamageOnly() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        Permanent chosen = addCreatureReady(player1, new MoggSentry());
        Permanent target = addCreatureReady(player2, new SlingshotGoblin());
        Permanent damageRecipient = addCreatureReady(player1, new HuntingDrake());

        addAbilityMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardDogs), null,
                target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(target), null,
                damageRecipient.getId());
        harness.passBothPriorities();

        assertThat(damageRecipient.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canChooseGuardDogsWhenItIsTheOnlyControlledPermanent() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        Permanent attacker = addCreatureReady(player2, new AuroraGriffin());

        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(guardDogs.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    void checksColorsAfterResponsesResolve() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        addCreatureReady(player1, new AuroraGriffin());
        Permanent attacker = addCreatureReady(player2, new SlingshotGoblin());

        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guardDogs.getId());

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    void gainingASharedColorAfterResolutionDoesNotCreatePrevention() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        addCreatureReady(player1, new AuroraGriffin());
        Permanent attacker = addCreatureReady(player2, new SlingshotGoblin());

        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guardDogs.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, attacker.getId());
        harness.passBothPriorities();

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat - 2);
    }

    @Test
    void losingTheSharedColorAfterResolutionDoesNotRemovePrevention() {
        addCreatureReady(player1, new GuardDogs());
        addCreatureReady(player1, new AuroraGriffin());
        Permanent chosen = addCreatureReady(player1, new MoggSentry());
        Permanent attacker = addCreatureReady(player2, new SlingshotGoblin());

        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, chosen.getId());
        harness.passBothPriorities();

        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    @Test
    void cannotChooseAnOpponentsPermanent() {
        Permanent guardDogs = addCreatureReady(player1, new GuardDogs());
        addCreatureReady(player1, new AuroraGriffin());
        Permanent attacker = addCreatureReady(player2, new AuroraGriffin());

        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, guardDogs.getId());
        int lifeBeforeCombat = gd.getLife(player1.getId());
        attackWithoutBlockers(attacker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCombat);
    }

    private void attackWithoutBlockers(Permanent attacker) {
        declareAttackersAndPrepareBlockers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
