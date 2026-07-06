package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LostVale;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DowsingDaggerTest extends BaseCardTest {

    // ===== Card structure =====

    

    

    

    @Test
    @DisplayName("Has equip {2} ability")
    void hasEquipAbility() {
        DowsingDagger card = new DowsingDagger();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    

    // ===== Static effects =====

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Creature loses boost when Dagger is removed")
    void creatureLosesBoostWhenRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(dagger);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== ETB: target opponent creates Plant tokens =====

    @Nested
    @DisplayName("ETB Plant token creation")
    class EtbPlantTokens {

        @Test
        @DisplayName("Casting Dowsing Dagger targeting opponent creates two 0/2 green Plant tokens with defender for opponent")
        void etbCreatesPlantTokensForOpponent() {
            harness.setHand(player1, new ArrayList<>(List.of(new DowsingDagger())));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castArtifact(player1, 0, player2.getId());
            harness.passBothPriorities(); // resolve artifact spell
            harness.passBothPriorities(); // resolve ETB trigger

            List<Permanent> opponentBattlefield = gd.playerBattlefields.get(player2.getId());
            List<Permanent> plants = opponentBattlefield.stream()
                    .filter(p -> p.getCard().getName().equals("Plant"))
                    .toList();
            assertThat(plants).hasSize(2);

            for (Permanent plant : plants) {
                assertThat(plant.getCard().getPower()).isEqualTo(0);
                assertThat(plant.getCard().getToughness()).isEqualTo(2);
                assertThat(plant.getCard().getColor()).isEqualTo(CardColor.GREEN);
                assertThat(plant.getCard().getSubtypes()).contains(CardSubtype.PLANT);
                assertThat(plant.getCard().getKeywords()).contains(Keyword.DEFENDER);
            }
        }

        @Test
        @DisplayName("Plant tokens are created under opponent's control, not controller's")
        void plantsUnderOpponentControl() {
            harness.setHand(player1, new ArrayList<>(List.of(new DowsingDagger())));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castArtifact(player1, 0, player2.getId());
            harness.passBothPriorities(); // resolve artifact spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Opponent should have the plants
            long opponentPlants = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Plant"))
                    .count();
            assertThat(opponentPlants).isEqualTo(2);

            // Controller should have no plants
            long controllerPlants = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Plant"))
                    .count();
            assertThat(controllerPlants).isZero();
        }
    }

    // ===== Combat damage: transform trigger =====

    @Nested
    @DisplayName("Combat damage transform trigger")
    class CombatDamageTransform {

        @Test
        @DisplayName("Equipped creature dealing combat damage offers may-transform choice")
        void combatDamageOffersMayTransform() {
            Permanent creature = addReadyCreature(player1);
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();

            // Should be awaiting may ability choice
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Accepting transform turns Dowsing Dagger into Lost Vale")
        void acceptingTransformCreatesLostVale() {
            Permanent creature = addReadyCreature(player1);
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();

            // Accept transform
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            // Dagger should now be Lost Vale
            assertThat(dagger.isTransformed()).isTrue();
            assertThat(dagger.getCard().getName()).isEqualTo("Lost Vale");
            assertThat(dagger.getCard()).isInstanceOf(LostVale.class);
        }

        @Test
        @DisplayName("Declining transform keeps Dowsing Dagger unchanged")
        void decliningTransformKeepsDagger() {
            Permanent creature = addReadyCreature(player1);
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();

            // Decline transform
            harness.handleMayAbilityChosen(player1, false);

            // Dagger should remain as Dowsing Dagger
            assertThat(dagger.isTransformed()).isFalse();
            assertThat(dagger.getCard().getName()).isEqualTo("Dowsing Dagger");
        }

        @Test
        @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
        void noTriggerWhenBlocked() {
            Permanent creature = addReadyCreature(player1);
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            // Add blocker with high toughness
            Permanent blocker = addReadyCreature(player2);
            blocker.getCard().setToughness(10);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            resolveCombat();

            // Should not be awaiting may ability — no combat damage to player
            assertThat(dagger.isTransformed()).isFalse();
            assertThat(dagger.getCard().getName()).isEqualTo("Dowsing Dagger");
        }
    }

    // ===== Lost Vale back face used after transform =====

    @Test
    @DisplayName("After transform, Lost Vale is on the battlefield as a land")
    void lostValeOnBattlefieldAfterTransform() {
        Permanent creature = addReadyCreature(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Accept transform
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Lost Vale should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lost Vale"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addDaggerReady(Player player) {
        Permanent perm = new Permanent(new DowsingDagger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLostValeReady(Player player) {
        DowsingDagger dagger = new DowsingDagger();
        Permanent perm = new Permanent(dagger);
        perm.setCard(dagger.getBackFaceCard());
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
