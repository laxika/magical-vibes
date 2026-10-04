package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltLeafsEmbrace.class, GrizzlyBears.class, FountainOfYouth.class,
        AuraGraft.class, Naturalize.class})
class GiltLeafsEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0 and gains trample and indestructible until end of turn")
    void enchantedCreatureGetsBoostAndTemporaryKeywords() {
        Permanent bears = castAuraOnCreature();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiltLeafsEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent castAuraOnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1,
                new GrizzlyBears());
        harness.setHand(player1, List.of(new GiltLeafsEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();
        return bears;
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        Permanent bears = castAuraOnCreature();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void removingAuraAfterTriggerResolvesRemovesOnlyPowerBoost() {
        Permanent bears = castAuraOnCreature();
        Permanent aura = findPermanent(player1, "Gilt-Leaf's Embrace");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void movingAuraBeforeEnterTriggerResolvesGrantsKeywordsToNewHost() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiltLeafsEmbrace(), new AuraGraft()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Gilt-Leaf's Embrace");
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, newHost.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newHost)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newHost, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newHost, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void enteringWithoutBeingCastGrantsKeywordsWithoutChoosingTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiltLeafsEmbrace());
        aura.setAttachedTo(bears.getId());
        harness.getBattlefieldEntryService().processCreatureETBEffects(
                gd, player1.getId(), aura.getCard(), null, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
