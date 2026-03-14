package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerpentOfTheEndlessSeaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Serpent of the Endless Sea has correct static effects")
    void hasCorrectProperties() {
        SerpentOfTheEndlessSea card = new SerpentOfTheEndlessSea();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);

        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(PowerToughnessEqualToControlledSubtypeCountEffect.class);
        PowerToughnessEqualToControlledSubtypeCountEffect ptEffect =
                (PowerToughnessEqualToControlledSubtypeCountEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(ptEffect.subtype()).isEqualTo(CardSubtype.ISLAND);

        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(CantAttackUnlessDefenderControlsMatchingPermanentEffect.class);
        CantAttackUnlessDefenderControlsMatchingPermanentEffect attackEffect =
                (CantAttackUnlessDefenderControlsMatchingPermanentEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(attackEffect.defenderPermanentPredicate()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.ISLAND));
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Serpent of the Endless Sea puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SerpentOfTheEndlessSea()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serpent of the Endless Sea");
    }

    // ===== P/T based on Islands =====

    @Test
    @DisplayName("Serpent dies to state-based actions with no Islands")
    void diesWithNoIslands() {
        harness.setHand(player1, List.of(new SerpentOfTheEndlessSea()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serpent of the Endless Sea"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serpent of the Endless Sea"));
    }

    @Test
    @DisplayName("Serpent survives when you control an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SerpentOfTheEndlessSea()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serpent of the Endless Sea"));
    }

    @Test
    @DisplayName("Serpent P/T equals number of Islands you control")
    void ptEqualsControlledIslands() {
        Permanent serpent = addSerpentReady(player1);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Serpent counts only your Islands, not opponent Islands")
    void countsOnlyControllersIslands() {
        Permanent serpent = addSerpentReady(player1);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Serpent P/T updates when Islands change")
    void ptUpdatesWhenIslandsChange() {
        Permanent serpent = addSerpentReady(player1);
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(1);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Island"));
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(0);
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Serpent can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new Island());

        Permanent serpent = addSerpentReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        gs.declareAttackers(gd, player1, List.of(serpentIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Serpent cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent serpent = addSerpentReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSerpentReady(Player player) {
        SerpentOfTheEndlessSea card = new SerpentOfTheEndlessSea();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
