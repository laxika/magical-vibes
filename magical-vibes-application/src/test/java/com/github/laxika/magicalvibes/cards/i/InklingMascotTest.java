package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InklingMascotTest extends BaseCardTest {

    private Permanent addReadyMascot(Player player) {
        Permanent perm = new Permanent(new InklingMascot());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== Structure =====

    @Test
    @DisplayName("Repartee grants self flying and surveils 1, gated on targeting a creature")
    void hasCorrectStructure() {
        InklingMascot card = new InklingMascot();

        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.castSpellTargetCondition()).isInstanceOf(StackEntryTargetsPermanentPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(2);
        assertThat(trigger.resolvedEffects().get(0)).isInstanceOf(GrantKeywordEffect.class);
        assertThat(trigger.resolvedEffects().get(1)).isInstanceOf(SurveilEffect.class);
    }

    // ===== Repartee: gains flying + surveil =====

    @Test
    @DisplayName("Casting an instant that targets a creature gives the Mascot flying and surveils")
    void reparteeGrantsFlyingAndSurveils() {
        Permanent mascot = addReadyMascot(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // resolve Repartee trigger — grants flying, queues surveil may
        harness.handleMayAbilityChosen(player1, true); // surveil: put top card into graveyard

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent mascot = addReadyMascot(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline surveil

        assertThat(mascot.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mascot.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        addReadyMascot(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
}
