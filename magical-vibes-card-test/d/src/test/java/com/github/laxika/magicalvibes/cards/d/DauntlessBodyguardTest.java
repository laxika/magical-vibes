package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SylvanAwakening;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntlessBodyguard.class, BalothGorger.class, Plains.class, SylvanAwakening.class})
class DauntlessBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with another creature prompts for creature choice")
    void castingWithOtherCreaturePromptsChoice() {
        addCreatureReady(player1, new BalothGorger());
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature stores chosenPermanentId on the bodyguard")
    void choosingCreatureStoresId() {
        Permanent bears = addCreatureReady(player1, new BalothGorger());
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard.getChosenPermanentId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Casting with no other creatures enters without choice prompt")
    void castingWithNoOtherCreaturesEntersNormally() {
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isFalse();
        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard).isNotNull();
        assertThat(bodyguard.getChosenPermanentId()).isNull();
    }

    @Test
    @DisplayName("Sacrificing bodyguard grants indestructible to chosen creature")
    void sacrificeGrantsIndestructibleToChosenCreature() {
        Permanent bears = addCreatureReady(player1, new BalothGorger());
        Permanent bodyguard = addCreatureReady(player1, new DauntlessBodyguard());
        bodyguard.setChosenPermanentId(bears.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        harness.assertNotOnBattlefield(player1, "Dauntless Bodyguard");
        harness.assertInGraveyard(player1, "Dauntless Bodyguard");
    }

    @Test
    @DisplayName("Sacrifice does nothing if no creature was chosen")
    void sacrificeDoesNothingWhenNoCreatureChosen() {
        Permanent bears = addCreatureReady(player1, new BalothGorger());
        addCreatureReady(player1, new DauntlessBodyguard());
        // chosenPermanentId is null (no creature chosen)

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.assertNotOnBattlefield(player1, "Dauntless Bodyguard");
    }

    @Test
    @DisplayName("Sacrifice does nothing if chosen creature left the battlefield")
    void sacrificeDoesNothingWhenChosenCreatureGone() {
        Permanent bears = addCreatureReady(player1, new BalothGorger());
        Permanent bodyguard = addCreatureReady(player1, new DauntlessBodyguard());
        bodyguard.setChosenPermanentId(bears.getId());

        // Remove the chosen creature before activating
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bodyguard was sacrificed but chosen creature is gone — nothing to grant indestructible to
        harness.assertNotOnBattlefield(player1, "Dauntless Bodyguard");
    }

    @Test
    @DisplayName("Full flow: cast bodyguard, choose creature, sacrifice for indestructible")
    void fullFlowCastChooseSacrifice() {
        Permanent bears = addCreatureReady(player1, new BalothGorger());
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Cast bodyguard
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Choose bears as the protected creature
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent bodyguard = findPermanent(player1, "Dauntless Bodyguard");
        assertThat(bodyguard.getChosenPermanentId()).isEqualTo(bears.getId());

        // Sacrifice bodyguard
        bodyguard.setSummoningSick(false);
        int bodyguardIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bodyguard);
        harness.activateAbility(player1, bodyguardIdx, null, null);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        harness.assertNotOnBattlefield(player1, "Dauntless Bodyguard");
    }

    @Test
    @DisplayName("The chosen permanent can be protected after it stops being a creature")
    void protectsChosenLandAfterAnimationExpires() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new SylvanAwakening(), new DauntlessBodyguard()));
        // Player2's default hand would exceed the maximum hand size at their turn-2 cleanup.
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gqs.isCreature(gd, plains)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, plains.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, plains)).isFalse();
        assertThat(gqs.hasKeyword(gd, plains, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Dauntless Bodyguard");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, plains, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The entry choice excludes itself and opposing creatures")
    void entryChoiceIncludesOnlyOtherOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
    }

    @Test
    @DisplayName("Indestructible expires at the end of the turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        harness.setHand(player1, List.of(new DauntlessBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Dauntless Bodyguard");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
