package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureToCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedThePackTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MayEffect wrapping sacrifice-create-tokens on CONTROLLER_END_STEP_TRIGGERED")
    void hasCorrectEffect() {
        FeedThePack card = new FeedThePack();

        var effects = card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(MayEffect.class);

        MayEffect may = (MayEffect) effects.getFirst();
        assertThat(may.wrapped()).isInstanceOf(SacrificeCreatureToCreateTokensEqualToToughnessEffect.class);

        SacrificeCreatureToCreateTokensEqualToToughnessEffect inner =
                (SacrificeCreatureToCreateTokensEqualToToughnessEffect) may.wrapped();

        // Token template is a 2/2 green Wolf
        CreateTokenEffect token = inner.tokenTemplate();
        assertThat(token.tokenName()).isEqualTo("Wolf");
        assertThat(token.power()).isEqualTo(2);
        assertThat(token.toughness()).isEqualTo(2);
        assertThat(token.color()).isEqualTo(CardColor.GREEN);
        assertThat(token.subtypes()).containsExactly(CardSubtype.WOLF);

        // Only nontoken creatures may be sacrificed
        assertThat(inner.filter()).isInstanceOf(PermanentNotPredicate.class);
        assertThat(((PermanentNotPredicate) inner.filter()).predicate())
                .isInstanceOf(PermanentIsTokenPredicate.class);
    }

    // ===== Accept: sacrifice creates wolves equal to toughness =====

    @Test
    @DisplayName("Accepting and sacrificing a 2/4 creature creates four 2/2 Wolf tokens")
    void acceptCreatesWolvesEqualToToughness() {
        harness.addToBattlefield(player1, new FeedThePack());
        harness.addToBattlefield(player1, new AzureDrake()); // 2/4
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Azure Drake"))
                .findFirst().orElse(null);
        assertThat(drake).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — trigger queues MayEffect onto the stack
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        // Resolve the triggered ability — MayEffect presents the may choice
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept — now must choose a creature to sacrifice
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, drake.getId());

        // Token creation is on the stack — resolve it
        harness.passBothPriorities();

        GameData gd2 = harness.getGameData();

        // Azure Drake was sacrificed
        assertThat(gd2.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Azure Drake"))).isFalse();
        assertThat(gd2.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Azure Drake"))).isTrue();

        // Four 2/2 green Wolf tokens were created (toughness 4)
        var wolves = gd2.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .toList();
        assertThat(wolves).hasSize(4);
        assertThat(wolves).allSatisfy(w -> {
            assertThat(w.getCard().getPower()).isEqualTo(2);
            assertThat(w.getCard().getToughness()).isEqualTo(2);
            assertThat(w.getCard().isToken()).isTrue();
        });
    }

    // ===== Decline: nothing happens =====

    @Test
    @DisplayName("Declining the trigger leaves the creature alive and creates no tokens")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new FeedThePack());
        harness.addToBattlefield(player1, new AzureDrake());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step → trigger queued
        harness.passBothPriorities(); // resolve trigger → may choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        GameData gd2 = harness.getGameData();

        // Azure Drake still on the battlefield, no Wolf tokens created
        assertThat(gd2.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Azure Drake"))).isTrue();
        assertThat(gd2.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Wolf"))).isTrue();
    }

    // ===== Accept with no creatures: nothing to sacrifice =====

    @Test
    @DisplayName("Accepting with no creatures to sacrifice creates no tokens")
    void acceptWithNoCreaturesDoesNothing() {
        harness.addToBattlefield(player1, new FeedThePack());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step → trigger queued
        harness.passBothPriorities(); // resolve trigger → may choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        GameData gd2 = harness.getGameData();

        // No creature to sacrifice — no choice prompt, no Wolf tokens
        assertThat(gd2.interaction.isAwaitingInput()).isFalse();
        assertThat(gd2.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Wolf"))).isTrue();
    }
}
