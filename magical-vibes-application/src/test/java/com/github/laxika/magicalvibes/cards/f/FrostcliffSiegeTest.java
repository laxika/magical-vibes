package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostcliffSiege.class, GrizzlyBears.class, Naturalize.class})
class FrostcliffSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Temur gives your creatures +1/+0, trample, and haste")
    void temurModeBuffsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndChoose("Temur");

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Jeskai draws once when multiple creatures deal combat damage to a player")
    void jeskaiModeDrawsOnceForMultipleDamageDealers() {
        castAndChoose("Jeskai");
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent firstAttacker = addAttacker();
        Permanent secondAttacker = addAttacker();
        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCombat + 1);
    }

    @Test
    @DisplayName("Temur does not use the Jeskai combat-damage ability")
    void temurModeDoesNotDrawFromCombatDamage() {
        castAndChoose("Temur");
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent attacker = addAttacker();
        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCombat);
    }

    private Permanent addAttacker() {
        return addCreatureReady(player1, new GrizzlyBears());
    }

    @Test
    void jeskaiDrawResolvesAfterSiegeIsDestroyed() {
        Permanent siege = castAndChoose("Jeskai");
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        Permanent attacker = addAttacker();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
            resolveCombat();
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player2, 0, siege.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Frostcliff Siege");
            resolveAllTriggers();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        });
    }

    @Test
    void temurDoesNotCreateCombatDamageTrigger() {
        castAndChoose("Temur");
        Permanent attacker = addAttacker();

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
            resolveCombat();
            harness.assertLife(player2, 17);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    void jeskaiDoesNotGrantTemurBonuses() {
        castAndChoose("Jeskai");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void temurAppliesToCreaturesEnteringLaterAndEndsWhenSiegeLeaves() {
        Permanent siege = castAndChoose("Temur");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, siege.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    private Permanent castAndChoose(String mode) {
        harness.setHand(player1, List.of(new FrostcliffSiege()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Jeskai", "Temur");
        harness.handleListChoice(player1, mode);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FrostcliffSiege)
                .findFirst()
                .orElseThrow();
    }
}
