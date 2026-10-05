package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetKeyrune.class, Forest.class})
class IzzetKeyruneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Izzet Keyrune adds one blue or red mana")
    void tappingAddsChosenMana() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying blue and red mana animates Izzet Keyrune")
    void payingBlueAndRedAnimatesKeyrune() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keyrune)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, keyrune)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, keyrune))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(keyrune.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Izzet Keyrune stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isFalse();
        assertThat(gqs.isArtifact(keyrune)).isTrue();
    }

    @Test
    @DisplayName("Combat damage lets the controller accept loot")
    void combatDamageAcceptsLoot() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        animateKeyrune();
        keyrune.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Combat damage lets the controller decline loot")
    void combatDamageDeclinesLoot() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        animateKeyrune();
        keyrune.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void tappingCanAddBlueMana() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedKeyruneCanAnimateWithoutUntapping() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, keyrune)).isTrue();
        assertThat(keyrune.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void lootCanDiscardAnExistingCardAndKeepTheDrawnCard() {
        Permanent keyrune = addCreatureReady(player1, new IzzetKeyrune());
        IzzetKeyrune originalCard = new IzzetKeyrune();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(originalCard));
        harness.setLibrary(player1, List.of(drawnCard));
        animateKeyrune();
        keyrune.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalCard, drawnCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(originalCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void animateKeyrune() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

}
