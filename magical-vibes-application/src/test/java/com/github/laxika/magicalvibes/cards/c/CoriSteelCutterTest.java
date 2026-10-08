package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.d.DragonstormForecaster;
import com.github.laxika.magicalvibes.cards.g.GhostlyFlicker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoriSteelCutter.class, LightningBolt.class, DragonstormForecaster.class, DoublingSeason.class,
        GhostlyFlicker.class})
class CoriSteelCutterTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell creates a Monk and offers to attach the Cutter to it")
    void secondSpellCreatesMonkAndOffersAttachment() {
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        castTwoLightningBolts();

        Permanent monk = monkTokens().getFirst();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cutter.getAttachedTo()).isEqualTo(monk.getId());
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The first spell does not create a Monk")
    void firstSpellDoesNotCreateMonk() {
        harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(monkTokens()).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A later noncreature spell gives the Monk its prowess boost")
    void laterNoncreatureSpellTriggersProwess() {
        harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        Permanent monk = monkTokens().getFirst();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Cutter attaches only to the Monk created by its own trigger")
    void multipleCuttersAttachToTheirOwnTokens() {
        Permanent firstCutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        Permanent secondCutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<UUID> attachedTokenIds = List.of(firstCutter.getAttachedTo(), secondCutter.getAttachedTo());
        Set<UUID> tokenIds = monkTokens().stream().map(Permanent::getId).collect(Collectors.toSet());
        assertThat(monkTokens()).hasSize(2);
        assertThat(attachedTokenIds).doesNotContainNull();
        assertThat(Set.copyOf(attachedTokenIds)).isEqualTo(tokenIds);
    }


    @Test
    @DisplayName("Declining attachment leaves the Monk unboosted and it can be equipped later")
    void declinedAttachmentCanBeEquippedLater() {
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        castTwoLightningBolts();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent monk = monkTokens().getFirst();
        assertThat(cutter.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.HASTE)).isFalse();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, monk.getId());
        harness.passBothPriorities();

        assertThat(cutter.getAttachedTo()).isEqualTo(monk.getId());
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Casting the Cutter as the first spell counts toward flurry")
    void cutterItselfCountsAsFirstSpell() {
        harness.setHand(player1, List.of(new CoriSteelCutter(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(monkTokens()).isEmpty();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monkTokens()).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Creature spells count for flurry but do not trigger the Monk's prowess")
    void creatureSpellsCountForFlurryButNotProwess() {
        harness.addToBattlefield(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new DragonstormForecaster(),
                new DragonstormForecaster(), new DragonstormForecaster()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(monkTokens()).isEmpty();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(monkTokens()).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent monk = monkTokens().getFirst();
        assertThat(monkTokens()).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's spells neither count for flurry nor trigger prowess")
    void opponentsSpellsDoNotCountOrTriggerProwess() {
        harness.addToBattlefield(player1, new CoriSteelCutter());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monkTokens()).isEmpty();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        Permanent monk = monkTokens().getFirst();
        assertThat(monkTokens()).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess expires and flurry can trigger again on an opponent's turn")
    void prowessExpiresAndFlurryResetsEachTurn() {
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        castTwoLightningBolts();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent firstMonk = monkTokens().getFirst();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monkTokens()).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, firstMonk)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, firstMonk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstMonk)).isEqualTo(2);

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(monkTokens()).hasSize(1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent secondMonk = monkTokens().stream()
                .filter(monk -> !monk.getId().equals(firstMonk.getId()))
                .findFirst().orElseThrow();
        assertThat(monkTokens()).hasSize(2);
        assertThat(cutter.getAttachedTo()).isEqualTo(secondMonk.getId());
        assertThat(gqs.getEffectivePower(gd, firstMonk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstMonk)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstMonk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, firstMonk, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondMonk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondMonk)).isEqualTo(2);
    }

    @Test
    @DisplayName("When token creation is doubled the controller may attach to either Monk")
    void doubledTokenCreationAllowsChoosingSecondMonk() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        castTwoLightningBolts();
        assertThat(monkTokens()).hasSize(2);
        Permanent chosenMonk = monkTokens().get(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrderElementsOf(
                monkTokens().stream().map(Permanent::getId).toList());
        harness.handlePermanentChosen(player1, chosenMonk.getId());

        assertThat(cutter.getAttachedTo()).isEqualTo(chosenMonk.getId());
        assertThat(gqs.getEffectivePower(gd, chosenMonk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, monkTokens().getFirst())).isEqualTo(1);
    }


    @Test
    @DisplayName("A Cutter that leaves and returns cannot be attached by its old flurry trigger")
    void blinkedCutterIsNotAttachedByOldTrigger() {
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        Permanent forecaster = harness.addToBattlefieldAndReturn(player1, new DragonstormForecaster());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, List.of(cutter.getId(), forecaster.getId()));

        Permanent returnedCutter = findPermanent(player1, "Cori-Steel Cutter");
        assertThat(returnedCutter.getId()).isNotEqualTo(cutter.getId());
        harness.passBothPriorities();
        assertThat(monkTokens()).hasSize(1);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(returnedCutter.getAttachedTo()).isNull();
        Permanent monk = monkTokens().getFirst();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.HASTE)).isFalse();
    }

    private void castTwoLightningBolts() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private List<Permanent> monkTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MONK))
                .toList();
    }
}
