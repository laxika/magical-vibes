package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.i.IxallisKeeper;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.cards.l.LostVale;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DowsingDagger.class, IxallisKeeper.class, AncientBrontodon.class, StrionicResonator.class})
class DowsingDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("Has equip {2} ability")
    void hasEquipAbility() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(dagger.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Creature loses boost when Dagger is removed")
    void creatureLosesBoostWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(dagger);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Nested
    @DisplayName("ETB Plant token creation")
    @CardUsed({DowsingDagger.class})
    class EtbPlantTokens {

        @Test
        @DisplayName("Casting Dowsing Dagger targeting opponent creates two 0/2 green Plant tokens with defender for opponent")
        void etbCreatesPlantTokensForOpponent() {
            harness.setHand(player1, List.of(new DowsingDagger()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castArtifact(player1, 0, player2.getId());
            harness.passBothPriorities(); // resolve artifact spell
            harness.passBothPriorities(); // resolve ETB trigger

            List<Permanent> plants = findPermanents(player2, "Plant");
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
            harness.setHand(player1, List.of(new DowsingDagger()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castArtifact(player1, 0, player2.getId());
            harness.passBothPriorities(); // resolve artifact spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Opponent should have the plants
            long opponentPlants = countPermanents(player2, "Plant");
            assertThat(opponentPlants).isEqualTo(2);

            // Controller should have no plants
            long controllerPlants = countPermanents(player1, "Plant");
            assertThat(controllerPlants).isZero();
        }
    }

    @Nested
    @DisplayName("Combat damage transform trigger")
    @CardUsed({DowsingDagger.class, IxallisKeeper.class, AncientBrontodon.class})
    class CombatDamageTransform {

        @Test
        @DisplayName("Equipped creature dealing combat damage offers may-transform choice")
        void combatDamageOffersMayTransform() {
            Permanent creature = addCreatureReady(player1, new IxallisKeeper());
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();
            resolveAllTriggers();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Accepting transform turns Dowsing Dagger into Lost Vale")
        void acceptingTransformCreatesLostVale() {
            Permanent creature = addCreatureReady(player1, new IxallisKeeper());
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();
            resolveAllTriggers();

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
            Permanent creature = addCreatureReady(player1, new IxallisKeeper());
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();
            resolveAllTriggers();

            // Decline transform
            harness.handleMayAbilityChosen(player1, false);

            // Dagger should remain as Dowsing Dagger
            assertThat(dagger.isTransformed()).isFalse();
            assertThat(dagger.getCard().getName()).isEqualTo("Dowsing Dagger");
        }

        @Test
        @DisplayName("No trigger when equipped creature is blocked and deals no player damage")
        void noTriggerWhenBlocked() {
            Permanent creature = addCreatureReady(player1, new IxallisKeeper());
            Permanent dagger = addDaggerReady(player1);
            dagger.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            // Add blocker with high toughness
            Permanent blocker = addCreatureReady(player2, new AncientBrontodon());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            resolveCombat();

            // Should not be awaiting may ability — no combat damage to player
            assertThat(dagger.isTransformed()).isFalse();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(dagger.getCard().getName()).isEqualTo("Dowsing Dagger");
        }
    }

    @Test
    @DisplayName("After transform, Lost Vale is on the battlefield as a land")
    void lostValeOnBattlefieldAfterTransform() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // Accept transform
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Lost Vale should be on the battlefield
        harness.assertOnBattlefield(player1, "Lost Vale");
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dagger.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dagger.getAttachedTo()).isNull();
    }

    @Test
    void unequippedCreatureDamageDoesNotTriggerTransformation() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(dagger.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void transformedValeImmediatelyAddsThreeManaOfOneColor(ManaColor color) {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        // Mana empties at the end of each step (CR 106.4), and auto-pass would otherwise end
        // END_OF_COMBAT as soon as Alice has no castable cards, so keep her stopped there.
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(dagger.isTransformed()).isTrue();
            assertThat(dagger.isTapped()).isFalse();
            assertThat(dagger.getAttachedTo()).isNull();
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
            assertThat(countPermanents(player2, "Plant")).isZero();

            harness.activateAbility(player1, 1, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(dagger.isTapped()).isTrue();
            for (ManaColor manaColor : ManaColor.values()) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                        .isEqualTo(manaColor == color ? 3 : 0);
            }
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @CardUsed({DowsingDagger.class, IxallisKeeper.class, StrionicResonator.class})
    void copiedTransformTriggerDoesNotTransformValeBack() {
        Permanent creature = addCreatureReady(player1, new IxallisKeeper());
        Permanent dagger = addDaggerReady(player1);
        harness.addToBattlefield(player1, new StrionicResonator());
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).hasSize(1);
        var triggerId = gd.stack.getFirst().getTargetableId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, triggerId);
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(dagger.isTransformed()).isTrue();
        assertThat(dagger.getCard()).isInstanceOf(LostVale.class);
    }

    private Permanent addDaggerReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DowsingDagger());
    }
}
