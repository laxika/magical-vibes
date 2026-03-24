package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KumenasSpeaker;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiverSneakTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has CantBeBlockedEffect as static effect and Merfolk-conditional boost trigger")
    void hasCorrectEffects() {
        RiverSneak card = new RiverSneak();

        // Static: can't be blocked
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CantBeBlockedEffect.class);

        // Trigger: Merfolk ETB gives +1/+1 until end of turn
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(SubtypeConditionalEffect.class);

        SubtypeConditionalEffect conditional =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.MERFOLK);
        assertThat(conditional.wrapped()).isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    // ===== Can't be blocked =====

    @Test
    @DisplayName("River Sneak cannot be blocked")
    void cannotBeBlocked() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent atkPerm = new Permanent(new RiverSneak());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    // ===== Merfolk trigger =====

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Merfolk enters")
    void getsBoostWhenMerfolkEnters() {
        harness.addToBattlefield(player1, new RiverSneak());

        Permanent riverSneak = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast Kumena's Speaker (Merfolk)
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (triggers River Sneak)
        harness.passBothPriorities(); // resolve River Sneak's boost triggered ability

        assertThat(gqs.getEffectivePower(gd, riverSneak)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, riverSneak)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Merfolk creature enters")
    void noBoostWhenNonMerfolkEnters() {
        harness.addToBattlefield(player1, new RiverSneak());

        Permanent riverSneak = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast Grizzly Bears (Bear, not Merfolk)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gqs.getEffectivePower(gd, riverSneak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, riverSneak)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when opponent casts a Merfolk")
    void noBoostWhenOpponentCastsMerfolk() {
        harness.addToBattlefield(player1, new RiverSneak());

        Permanent riverSneak = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Opponent casts a Merfolk
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new KumenasSpeaker()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gqs.getEffectivePower(gd, riverSneak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, riverSneak)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets multiple boosts from multiple Merfolk entries")
    void getsMultipleBoosts() {
        harness.addToBattlefield(player1, new RiverSneak());

        Permanent riverSneak = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast first Merfolk
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve River Sneak's triggered ability

        assertThat(gqs.getEffectivePower(gd, riverSneak)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, riverSneak)).isEqualTo(2);

        // Cast second Merfolk
        harness.setHand(player1, List.of(new KumenasSpeaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve River Sneak's triggered ability

        assertThat(gqs.getEffectivePower(gd, riverSneak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, riverSneak)).isEqualTo(3);
    }
}
