package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AysenBureaucrats;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InstillEnergy.class, AysenBureaucrats.class, Forest.class, GrizzlyBears.class})
class InstillEnergyTest extends BaseCardTest {
    @Test
    @DisplayName("Summoning-sick creature enchanted with Instill Energy can attack")
    void enchantedSummoningSickCreatureCanAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(true);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(bearsPerm.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick creature without Instill Energy cannot attack")
    void summoningSickCreatureCannotAttackWithoutAura() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Instill Energy can be cast targeting a creature")
    void resolvingInstillEnergyAttachesToTargetCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InstillEnergy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        Permanent auraPerm = findPermanent(player1, "Instill Energy");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(bearsPerm.getId());
    }

    @Test
    @DisplayName("Instill Energy cannot be cast targeting a land")
    void cannotEnchantALand() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent forestPerm = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new InstillEnergy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Instill Energy does not let a summoning-sick creature activate a tap ability")
    void attackPermissionDoesNotGrantHasteForTapAbilities() {
        Permanent bureaucrats = harness.addToBattlefieldAndReturn(player1, new AysenBureaucrats());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bureaucrats.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
    @Test
    @DisplayName("Activated ability untaps the enchanted creature")
    void activatedAbilityUntapsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can only be activated once each turn")
    void untapAbilityOnlyOncePerTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        bearsPerm.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Untap ability can only be activated during your turn")
    void untapAbilityOnlyDuringYourTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Untap ability can be activated again on a later turn")
    void untapAbilityCanBeActivatedAgainOnNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        bearsPerm.tap();
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        bearsPerm.tap();

        assertThatCode(() -> harness.activateAbility(player1, 1, null, null))
                .doesNotThrowAnyException();
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can untap an enchanted creature controlled by an opponent")
    void activatedAbilityUntapsOpponentsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        bearsPerm.tap();
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }
}
