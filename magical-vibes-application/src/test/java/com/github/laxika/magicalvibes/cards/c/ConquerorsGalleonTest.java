package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndOfCombatAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConquerorsGalleonTest extends BaseCardTest {

    // ===== Front face card structure =====

    @Test
    @DisplayName("Front face has ON_ATTACK effect for exile-and-return-transformed")
    void hasAttackTrigger() {
        ConquerorsGalleon card = new ConquerorsGalleon();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(ExileSelfAtEndOfCombatAndReturnTransformedEffect.class);
    }

    @Test
    @DisplayName("Front face has Crew 4 activated ability")
    void hasCrewAbility() {
        ConquerorsGalleon card = new ConquerorsGalleon();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CrewCost.class);
        assertThat(((CrewCost) ability.getEffects().get(0)).requiredPower()).isEqualTo(4);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateSelfAsCreatureEffect.class);
    }

    @Test
    @DisplayName("Front face has back face set to ConquerorsFoothold")
    void hasBackFace() {
        ConquerorsGalleon card = new ConquerorsGalleon();

        assertThat(card.getBackFaceClassName()).isEqualTo("ConquerorsFoothold");
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(ConquerorsFoothold.class);
    }

    // ===== Back face card structure =====

    @Test
    @DisplayName("Back face has tap-for-mana on ON_TAP slot")
    void backFaceHasManaAbility() {
        ConquerorsFoothold card = new ConquerorsFoothold();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isInstanceOf(AwardManaEffect.class);
    }

    @Test
    @DisplayName("Back face has three activated abilities")
    void backFaceHasActivatedAbilities() {
        ConquerorsFoothold card = new ConquerorsFoothold();

        assertThat(card.getActivatedAbilities()).hasSize(3);

        // {2}, {T}: Draw a card, then discard a card.
        var lootAbility = card.getActivatedAbilities().get(0);
        assertThat(lootAbility.isRequiresTap()).isTrue();
        assertThat(lootAbility.getManaCost()).isEqualTo("{2}");
        assertThat(lootAbility.getEffects().getFirst()).isInstanceOf(DrawAndDiscardCardEffect.class);

        // {4}, {T}: Draw a card.
        var drawAbility = card.getActivatedAbilities().get(1);
        assertThat(drawAbility.isRequiresTap()).isTrue();
        assertThat(drawAbility.getManaCost()).isEqualTo("{4}");
        assertThat(drawAbility.getEffects().getFirst()).isInstanceOf(DrawCardEffect.class);

        // {6}, {T}: Return target card from your graveyard to your hand.
        var returnAbility = card.getActivatedAbilities().get(2);
        assertThat(returnAbility.isRequiresTap()).isTrue();
        assertThat(returnAbility.getManaCost()).isEqualTo("{6}");
        assertThat(returnAbility.getEffects().getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Attack → exile → return transformed =====

    @Nested
    @DisplayName("Attack and transform")
    class AttackAndTransform {

        @Test
        @DisplayName("Attacking with Galleon exiles it at end of combat and returns transformed as Foothold")
        void attackExilesAndReturnsTransformed() {
            Permanent galleon = addGalleonReady(player1);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            // Resolve the ON_ATTACK trigger (schedules exile at end of combat)
            harness.passBothPriorities();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Galleon should be gone, Foothold should be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Conqueror's Galleon"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Conqueror's Foothold"));

            // The new permanent should be marked as transformed
            Permanent foothold = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Conqueror's Foothold"))
                    .findFirst().orElseThrow();
            assertThat(foothold.isTransformed()).isTrue();
        }

        @Test
        @DisplayName("If Galleon leaves the battlefield before end of combat, it does not return")
        void noReturnIfAlreadyGone() {
            Permanent galleon = addGalleonReady(player1);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            // Resolve the ON_ATTACK trigger
            harness.passBothPriorities();

            // Remove the Galleon before end of combat (e.g. destroyed in combat)
            gd.playerBattlefields.get(player1.getId()).clear();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // No permanents should be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Foothold returns under controller's control even if Galleon was stolen")
        void returnsUnderControllerControl() {
            // The Galleon is controlled by player1
            Permanent galleon = addGalleonReady(player1);
            animateAsCreature(galleon);

            declareAttackers(List.of(0));
            harness.passBothPriorities();

            // Advance to END_OF_COMBAT
            harness.forceStep(TurnStep.END_OF_COMBAT);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Foothold should be on player1's battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Conqueror's Foothold"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Conqueror's Foothold"));
        }
    }

    // ===== Conqueror's Foothold abilities =====

    @Nested
    @DisplayName("Conqueror's Foothold abilities")
    class FootholdAbilities {

        @Test
        @DisplayName("{2}, {T}: Draw a card, then discard a card (loot)")
        void lootAbility() {
            Permanent foothold = addFootholdReady(player1);
            setDeck(player1, List.of(new GrizzlyBears()));
            harness.setHand(player1, new ArrayList<>(List.of(new Shock())));

            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Drew a card, now awaiting discard choice
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
            harness.handleCardChosen(player1, 0);

            // Hand size should be 1 (started with 1, drew 1, discarded 1)
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(foothold.isTapped()).isTrue();
        }

        @Test
        @DisplayName("{4}, {T}: Draw a card")
        void drawAbility() {
            Permanent foothold = addFootholdReady(player1);
            setDeck(player1, List.of(new GrizzlyBears()));
            harness.setHand(player1, new ArrayList<>());

            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(foothold.isTapped()).isTrue();
        }

        @Test
        @DisplayName("{6}, {T}: Return target card from graveyard to hand")
        void returnFromGraveyardAbility() {
            Permanent foothold = addFootholdReady(player1);
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));

            harness.addMana(player1, ManaColor.COLORLESS, 6);
            harness.activateAbility(player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(foothold.isTapped()).isTrue();
        }
    }

    // ===== Helpers =====

    private Permanent addGalleonReady(Player player) {
        Permanent perm = new Permanent(new ConquerorsGalleon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addFootholdReady(Player player) {
        ConquerorsGalleon galleon = new ConquerorsGalleon();
        Permanent perm = new Permanent(galleon);
        perm.setCard(galleon.getBackFaceCard());
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void animateAsCreature(Permanent perm) {
        perm.setAnimatedUntilEndOfTurn(true);
        perm.setAnimatedPower(2);
        perm.setAnimatedToughness(10);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
