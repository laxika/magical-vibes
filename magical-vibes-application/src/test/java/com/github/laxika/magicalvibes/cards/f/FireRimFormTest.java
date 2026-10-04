package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FireRimForm.class, FountainOfYouth.class, GrizzlyBears.class, AuraGraft.class})
class FireRimFormTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fire-Rim Form attaches it, boosts the creature, and grants first strike")
    void resolvingAttachesAndAppliesEffects() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FireRimForm
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by Fire-Rim Form wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fire-Rim Form cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The +2/+0 bonus ends when Fire-Rim Form leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new FireRimForm());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn, with first strike granted by a separate trigger")
    void flashOnOpponentsTurnResolvesAuraBeforeTrigger() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fire-Rim Form").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The enter trigger grants first strike to the creature enchanted when it resolves")
    void movingAuraBeforeTriggerResolvesChangesRecipient() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireRimForm(), new AuraGraft()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Fire-Rim Form");
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isFalse();

        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, replacement.getId());
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(4);
    }

    @Test
    @DisplayName("First strike already granted persists when the Aura leaves the battlefield")
    void grantedFirstStrikePersistsWithoutAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        Permanent aura = findPermanent(player1, "Fire-Rim Form");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An Aura whose creature leaves before resolution does not enter or grant first strike")
    void missingTargetPreventsAuraEntering() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireRimForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fire-Rim Form");
        harness.assertInGraveyard(player1, "Fire-Rim Form");
        assertThat(gd.stack).isEmpty();
    }
}
