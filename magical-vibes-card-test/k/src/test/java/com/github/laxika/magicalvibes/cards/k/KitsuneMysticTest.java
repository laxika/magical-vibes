package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AutumnTailKitsuneSage;
import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.IndomitableWill;
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

@CardUsed({KitsuneMystic.class, AutumnTailKitsuneSage.class, DevotedRetainer.class, IndomitableWill.class,
        HumbleBudoka.class, BoundByMoonsilver.class})
class KitsuneMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Flips at the end step when enchanted by two Auras")
    void flipsWithTwoAuras() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips at the end step when enchanted by more than two Auras")
    void flipsWithMoreThanTwoAuras() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        addAuraAttachedTo(player1, mystic);
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips during an opponent's end step when it has two Auras")
    void flipsDuringOpponentsEndStep() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip at the end step with fewer than two Auras")
    void doesNotFlipWithOneAura() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not flip if the Aura condition is lost before resolution")
    void doesNotFlipIfConditionIsLostBeforeResolution() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        Permanent secondAura = addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(secondAura);
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Autumn-Tail moves an Aura to another creature")
    void autumnTailMovesAura() {
        Permanent mystic = addMystic();
        Permanent firstCreature = addCreatureReady(player1, new DevotedRetainer());
        Permanent secondCreature = addCreatureReady(player1, new DevotedRetainer());
        Permanent aura = addAuraAttachedTo(player1, firstCreature);
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, secondCreature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Autumn-Tail can be activated without choosing a destination creature")
    void autumnTailChoosesDestinationOnlyOnResolution() {
        Permanent mystic = addMystic();
        Permanent creature = addCreatureReady(player1, new DevotedRetainer());
        Permanent aura = addAuraAttachedTo(player1, creature);
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, aura.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Autumn-Tail requires an Aura attached to a creature as its first target")
    void autumnTailRequiresAttachedAuraTarget() {
        Permanent mystic = addMystic();
        Permanent unattachedAura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, unattachedAura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({HumbleBudoka.class})
    @DisplayName("Autumn-Tail can move an Aura onto a creature with shroud")
    void autumnTailMovesAuraOntoShroudedCreature() {
        Permanent mystic = addMystic();
        Permanent firstCreature = addCreatureReady(player1, new DevotedRetainer());
        Permanent destination = addCreatureReady(player2, new HumbleBudoka());
        Permanent aura = addAuraAttachedTo(player1, firstCreature);
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, destination.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
    }

    @Test
    @CardUsed({BoundByMoonsilver.class})
    @DisplayName("Preventing transformation does not prevent Kitsune Mystic from flipping")
    void flipsWhileEnchantedByBoundByMoonsilver() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        Permanent binding = harness.addToBattlefieldAndReturn(player2, new BoundByMoonsilver());
        binding.setAttachedTo(mystic.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Auras controlled by opponents count toward the flip condition")
    void flipsWithOpponentsAuras() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player2, mystic);
        addAuraAttachedTo(player2, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
    }

    private Permanent addMystic() {
        return addCreatureReady(player1, new KitsuneMystic());
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new IndomitableWill());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
