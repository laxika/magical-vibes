package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapSubtypeBoostSelfAndDamageDefenderEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MyrBattlesphereTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Myr Battlesphere has ETB token creation and ON_ATTACK trigger")
    void hasCorrectEffects() {
        MyrBattlesphere card = new MyrBattlesphere();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        CreateCreatureTokenEffect tokenEffect =
                (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(4);
        assertThat(tokenEffect.tokenName()).isEqualTo("Myr");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isNull();
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.MYR);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(TapSubtypeBoostSelfAndDamageDefenderEffect.class);
        TapSubtypeBoostSelfAndDamageDefenderEffect attackEffect =
                (TapSubtypeBoostSelfAndDamageDefenderEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(attackEffect.subtype()).isEqualTo(CardSubtype.MYR);
    }

    // ===== ETB token creation =====

    @Test
    @DisplayName("ETB creates four 1/1 colorless Myr artifact creature tokens")
    void etbCreatesFourMyrTokens() {
        harness.setHand(player1, List.of(new MyrBattlesphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(5); // 1 Battlesphere + 4 Myr tokens
        assertThat(countMyrTokens()).isEqualTo(4);
    }

    @Test
    @DisplayName("Myr tokens are artifact creatures")
    void myrTokensAreArtifactCreatures() {
        harness.setHand(player1, List.of(new MyrBattlesphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent myrToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .findFirst().orElseThrow();
        assertThat(myrToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(myrToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myrToken.getCard().getSubtypes()).contains(CardSubtype.MYR);
    }

    // ===== Attack trigger =====

    @Test
    @DisplayName("Attacking with Myr Battlesphere pushes attack trigger onto stack")
    void attackTriggerPushesOntoStack() {
        setupBattlefieldWithMyr(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0)); // Battlesphere attacks

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Battlesphere");
    }

    @Test
    @DisplayName("Attack trigger resolution prompts multi-permanent choice for untapped Myr")
    void attackTriggerPromptsMultiPermanentChoice() {
        setupBattlefieldWithMyr(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Tapping Myr boosts Battlesphere and deals damage to defending player")
    void tappingMyrBoostsAndDealsDamage() {
        setupBattlefieldWithMyr(3);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        // Choose all 3 Myr tokens to tap
        List<UUID> myrIds = getMyrTokenIds(3);
        harness.handleMultiplePermanentsChosen(player1, myrIds);

        // Battlesphere should be boosted by +3/+0
        Permanent battlesphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(3);
        assertThat(battlesphere.getToughnessModifier()).isEqualTo(0);

        // Defending player takes 3 trigger damage + 7 combat damage (4 base + 3 boost) = 10
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);

        // Myr tokens should be tapped
        for (UUID myrId : myrIds) {
            Permanent myr = gqs.findPermanentById(gd, myrId);
            assertThat(myr.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Choosing zero Myr does not boost or deal trigger damage")
    void choosingZeroMyrDoesNothing() {
        setupBattlefieldWithMyr(2);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        // Choose zero Myr
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Battlesphere should not be boosted
        Permanent battlesphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(0);

        // No trigger damage, but Battlesphere still deals 4 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Tapping only some Myr boosts and damages by that amount")
    void tappingSomeMyrBoostsPartially() {
        setupBattlefieldWithMyr(4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        // Choose only 2 out of 4 Myr
        List<UUID> allMyrIds = getMyrTokenIds(4);
        List<UUID> selectedIds = allMyrIds.subList(0, 2);
        harness.handleMultiplePermanentsChosen(player1, new ArrayList<>(selectedIds));

        // Battlesphere should be boosted by +2/+0
        Permanent battlesphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(2);

        // Defending player takes 2 trigger damage + 6 combat damage (4 base + 2 boost) = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Battlesphere itself is not eligible for tapping since it taps to attack")
    void battlesphereNotEligibleForTapping() {
        // Only the Battlesphere on the battlefield, no Myr tokens
        Permanent battlesphere = new Permanent(new MyrBattlesphere());
        battlesphere.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(battlesphere);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.setLife(player2, 20);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger — no untapped Myr to tap

        // No multi-permanent choice should be prompted since the only Myr is tapped
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        // No trigger damage or boost, but Battlesphere still deals 4 combat damage
        assertThat(battlesphere.getPowerModifier()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Already tapped Myr are not eligible for tapping")
    void alreadyTappedMyrNotEligible() {
        setupBattlefieldWithMyr(3);

        // Tap two of the Myr tokens before combat
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.get(1).tap();
        battlefield.get(2).tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.setLife(player2, 20);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        // Should only have 1 untapped Myr as eligible choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        // Tap the one remaining untapped Myr
        UUID untappedMyrId = battlefield.get(3).getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(untappedMyrId));

        Permanent battlesphere = battlefield.getFirst();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(1);
        // 1 trigger damage + 5 combat damage (4 base + 1 boost) = 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Helper methods =====

    /**
     * Sets up a battlefield with a non-summoning-sick Myr Battlesphere at index 0
     * and the specified number of untapped Myr tokens.
     */
    private void setupBattlefieldWithMyr(int myrCount) {
        Permanent battlesphere = new Permanent(new MyrBattlesphere());
        battlesphere.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(battlesphere);

        for (int i = 0; i < myrCount; i++) {
            Card myrToken = new Card();
            myrToken.setName("Myr");
            myrToken.setType(CardType.CREATURE);
            myrToken.setManaCost("");
            myrToken.setToken(true);
            myrToken.setColor(null);
            myrToken.setPower(1);
            myrToken.setToughness(1);
            myrToken.setSubtypes(List.of(CardSubtype.MYR));
            myrToken.setAdditionalTypes(java.util.Set.of(CardType.ARTIFACT));

            Permanent myrPermanent = new Permanent(myrToken);
            myrPermanent.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(myrPermanent);
        }
    }

    private int countMyrTokens() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MYR))
                .count();
    }

    private List<UUID> getMyrTokenIds(int count) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .filter(p -> !p.isTapped())
                .limit(count)
                .map(Permanent::getId)
                .toList();
    }
}
