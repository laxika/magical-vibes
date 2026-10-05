package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.TrueConviction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrBattlesphere.class, KothOfTheHammer.class, Shatter.class, TrueConviction.class})
class MyrBattlesphereTest extends BaseCardTest {

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
        for (Permanent token : findPermanents(player1, "Myr")) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
        }
    }

    @Test
    @DisplayName("Myr tokens are artifact creatures")
    void myrTokensAreArtifactCreatures() {
        harness.setHand(player1, List.of(new MyrBattlesphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent myrToken = findPermanent(player1, "Myr");
        assertThat(myrToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(myrToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myrToken.getCard().getSubtypes()).contains(CardSubtype.MYR);
    }

    @Test
    @DisplayName("Attacking with Myr Battlesphere pushes attack trigger onto stack")
    void attackTriggerPushesOntoStack() {
        setupBattlefieldWithMyr(2);

        declareAttackers(List.of(0)); // Battlesphere attacks

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Battlesphere");
    }

    @Test
    @DisplayName("Attack trigger resolution prompts multi-permanent choice for untapped Myr")
    void attackTriggerPromptsMultiPermanentChoice() {
        setupBattlefieldWithMyr(2);

        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Tapping Myr boosts Battlesphere and deals damage to defending player")
    void tappingMyrBoostsAndDealsDamage() {
        setupBattlefieldWithMyr(3);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
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
        Permanent battlesphere = addCreatureReady(player1, new MyrBattlesphere());

        harness.setLife(player2, 20);
        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger — no untapped Myr to tap

        // No multi-permanent choice should be prompted since the only Myr is tapped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();

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

        harness.setLife(player2, 20);
        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger

        // Should only have 1 untapped Myr as eligible choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        // Tap the one remaining untapped Myr
        UUID untappedMyrId = battlefield.get(3).getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(untappedMyrId));

        Permanent battlesphere = battlefield.getFirst();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(1);
        // 1 trigger damage + 5 combat damage (4 base + 1 boost) = 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Completing the Myr choice finishes the parked attack-trigger resolution")
    void completingMyrChoiceFinishesParkedAttackTriggerResolution() {
        setupBattlefieldWithMyr(1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingEffectResolutionEntry)
                .as("the attack trigger is parked while its Myr choice is active")
                .isNotNull();
        assertThat(gd.pendingEffectResolutionEntry.getCard().getName()).isEqualTo("Myr Battlesphere");

        harness.handleMultiplePermanentsChosen(player1, getMyrTokenIds(1));

        assertThat(gd.pendingEffectResolutionEntry)
                .as("answering the choice must resume and finish the attack trigger")
                .isNull();
        assertThat(gd.pendingEffectResolutionIndex).isZero();
    }

    @Test
    @DisplayName("Attack ability damages the attacked planeswalker instead of its controller")
    void attackAbilityDamagesAttackedPlaneswalker() {
        setupBattlefieldWithMyr(2);
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, koth.getId()));
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, getMyrTokenIds(2)));

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lifelink gains life for damage dealt by the attack ability")
    void lifelinkAppliesToAttackAbilityDamage() {
        setupBattlefieldWithMyr(3);
        harness.addToBattlefield(player1, new TrueConviction());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, getMyrTokenIds(3)));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Attack ability still taps Myr and deals damage after Battlesphere is destroyed")
    void attackAbilityDealsDamageAfterSourceIsDestroyed() {
        setupBattlefieldWithMyr(3);
        Permanent battlesphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        List<UUID> myrIds = getMyrTokenIds(3);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, battlesphere.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentById(gd, battlesphere.getId())).isNull();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, myrIds);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        for (UUID myrId : myrIds) {
            assertThat(gqs.findPermanentById(gd, myrId).isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Another Battlesphere can be tapped, but an opponent's Myr cannot")
    void canTapNontokenMyrControlledByAbilityController() {
        Permanent attacker = addCreatureReady(player1, new MyrBattlesphere());
        Permanent otherMyr = harness.addToBattlefieldAndReturn(player1, new MyrBattlesphere());
        Permanent opposingMyr = harness.addToBattlefieldAndReturn(player2, new MyrBattlesphere());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(otherMyr.getId())));

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherMyr.isTapped()).isTrue();
        assertThat(opposingMyr.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Summoning sick Myr tokens can be tapped for the attack ability")
    void canTapSummoningSickMyrTokens() {
        setupBattlefieldWithMyr(2);
        List<UUID> myrIds = getMyrTokenIds(2);
        for (UUID myrId : myrIds) {
            gqs.findPermanentById(gd, myrId).setSummoningSick(true);
        }

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, myrIds));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        for (UUID myrId : myrIds) {
            assertThat(gqs.findPermanentById(gd, myrId).isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Battlesphere can tap itself if it is untapped before the attack ability resolves")
    void untappedAttackingBattlesphereCanTapItself() {
        Permanent battlesphere = addCreatureReady(player1, new MyrBattlesphere());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        battlesphere.untap();
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(battlesphere.getId())));

        assertThat(battlesphere.isTapped()).isTrue();
        assertThat(battlesphere.getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
    /**
     * Sets up a battlefield with a non-summoning-sick Myr Battlesphere at index 0
     * and the specified number of untapped Myr tokens.
     */
    private void setupBattlefieldWithMyr(int myrCount) {
        addCreatureReady(player1, new MyrBattlesphere());

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

            addCreatureReady(player1, myrToken);
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
