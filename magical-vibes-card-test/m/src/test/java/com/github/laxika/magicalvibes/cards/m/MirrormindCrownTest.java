package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshlingRekindled;
import com.github.laxika.magicalvibes.cards.a.AshlingRimebound;
import com.github.laxika.magicalvibes.cards.c.ChitinousGraspling;
import com.github.laxika.magicalvibes.cards.c.ClachanFestival;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.cards.r.RecklessRansacking;
import com.github.laxika.magicalvibes.cards.s.SyggsCommand;
import com.github.laxika.magicalvibes.cards.w.WoodlandChampion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrormindCrown.class, GrizzlyBears.class, JadeMage.class, ChitinousGraspling.class,
        ClachanFestival.class, MutableExplorer.class, RecklessRansacking.class, SyggsCommand.class,
        WoodlandChampion.class, AshlingRekindled.class, AshlingRimebound.class})
class MirrormindCrownTest extends BaseCardTest {

    @Test
    @DisplayName("The first token creation each turn may create copies of the equipped creature")
    void replacesFirstTokenCreationWithEquippedCreatureCopies() {
        setupCrownAndJadeMage();
        addJadeMageActivationMana();

        activateJadeMage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Declining the replacement creates the original tokens and uses the Crown for the turn")
    void declineCreatesOriginalTokensAndDoesNotOfferAgain() {
        setupCrownAndJadeMage();
        addJadeMageActivationMana();

        activateJadeMage();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        activateJadeMage();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Saproling"))
                .hasSize(2);
    }

    @Test
    void equipAttachesToOwnCreatureForTwoMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(crown.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void equipRejectsAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ChitinousGraspling());
        harness.addToBattlefield(player1, new MirrormindCrown());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedOutsideAMainPhase() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        harness.addToBattlefield(player1, new MirrormindCrown());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replacesEveryTokenInTheFirstCreationEvent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new ClachanFestival());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Chitinous Graspling");
                    assertThat(gqs.hasKeyword(gd, token, Keyword.REACH)).isTrue();
                    assertThat(gqs.getEffectiveColors(gd, token))
                            .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
                });
    }

    @Test
    void doesNotReplaceTokensAfterTheFirstAcceptedEvent() {
        setupCrownAndFestival();
        addFestivalActivationMana(2);

        activateFestival();
        harness.handleMayAbilityChosen(player1, true);
        activateFestival();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Chitinous Graspling", "Kithkin");
    }

    @Test
    void cannotReplaceLaterTokensAfterBeingEquippedMidTurn() {
        Permanent crown = setupCrownAndFestival();
        crown.setAttachedTo(null);
        addFestivalActivationMana(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateFestival();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.activateAbility(player1, 1, null,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();
        activateFestival();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allMatch(token -> token.getCard().getName().equals("Kithkin"));
    }

    @Test
    void cannotReplaceLaterTokensAfterEnteringMidTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        harness.addToBattlefield(player1, new ClachanFestival());
        addFestivalActivationMana(2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new MirrormindCrown()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 3, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allMatch(token -> token.getCard().getName().equals("Kithkin"));
    }

    @Test
    void multipleCrownsCannotBeSavedForSeparateEventsInOneTurn() {
        Permanent firstCrown = setupCrownAndFestival();
        Permanent secondCrown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        secondCrown.setAttachedTo(firstCrown.getAttachedTo());
        addFestivalActivationMana(2);

        activateFestival();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        for (int choice = 0; choice < 2
                && gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice; choice++) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        activateFestival();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Chitinous Graspling", "Kithkin");
    }

    @Test
    void replacementAlsoAppliesToCreatingTokenCopies() {
        Permanent equipped = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        Permanent otherMerfolk = harness.addToBattlefieldAndReturn(player1, new MutableExplorer());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(equipped.getId());
        harness.setHand(player1, List.of(new SyggsCommand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(otherMerfolk.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Chitinous Graspling"));
    }

    @Test
    void replacingTappedTokenCreationStillCreatesATappedToken() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new MutableExplorer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Chitinous Graspling");
                    assertThat(token.isTapped()).isTrue();
                });
    }

    @Test
    void copiesDoNotInheritCountersTappingOrTemporaryBoosts() {
        Permanent crown = setupCrownAndFestival();
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();
        harness.setHand(player1, List.of(new RecklessRansacking()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, crown.getAttachedTo());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
                });
    }

    @Test
    void replacementResetsAndCanBeUsedDuringTheOpponentsTurn() {
        Permanent crown = setupCrownAndFestival();
        addFestivalActivationMana(1);
        harness.setLibrary(player2, List.of(new ChitinousGraspling()));
        activateFestival();
        harness.handleMayAbilityChosen(player1, false);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RecklessRansacking()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, crown.getAttachedTo());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kithkin", "Chitinous Graspling");
    }

    @Test
    void opponentsTokenCreationDoesNotUseTheControllersReplacement() {
        setupCrownAndFestival();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ChitinousGraspling());
        harness.setHand(player2, List.of(new RecklessRansacking()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Treasure");

        harness.ensurePriority(player1);
        addFestivalActivationMana(1);
        activateFestival();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Chitinous Graspling");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void replacementCopiesStillTriggerAbilitiesThatWatchTokensEntering() {
        setupCrownAndFestival();
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        addFestivalActivationMana(1);

        activateFestival();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tokenCopiesRetainTheEquippedCreaturesActivatedAbilities() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new JadeMage());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(mage.getId());
        harness.addToBattlefield(player1, new ClachanFestival());
        addFestivalActivationMana(1);
        activateFestival();
        harness.handleMayAbilityChosen(player1, true);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 3, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Jade Mage", "Saproling");
    }

    @Test
    void copiedCreaturesEnterAbilitiesTriggerNormally() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MutableExplorer());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new ClachanFestival());
        addFestivalActivationMana(1);

        activateFestival();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Mutable Explorer", "Mutavault");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anEquippedTokenCanBeCopiedOnTheNextTurn() {
        harness.enterBattlefieldAndReturn(player1, new ClachanFestival());
        harness.passBothPriorities();
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.setLibrary(player2, List.of(new ChitinousGraspling()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(originalToken.getId());
        harness.ensurePriority(player1);
        addFestivalActivationMana(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Kithkin");
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    void copiesOfDoubleFacedCreaturesCanTransform() {
        Permanent ashling = harness.addToBattlefieldAndReturn(player2, new AshlingRekindled());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(ashling.getId());
        harness.addToBattlefield(player1, new ClachanFestival());
        addFestivalActivationMana(1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(token.isTransformed()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Ashling, Rimebound");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
    }

    private Permanent setupCrownAndFestival() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new ClachanFestival());
        return crown;
    }

    private void addFestivalActivationMana(int activations) {
        harness.addMana(player1, ManaColor.WHITE, activations);
        harness.addMana(player1, ManaColor.COLORLESS, 4 * activations);
    }

    private void activateFestival() {
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
    }

    private void setupCrownAndJadeMage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new MirrormindCrown());
        crown.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new JadeMage());
    }

    private void addJadeMageActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateJadeMage() {
        harness.activateAbility(player1, 2, 0, null);
        harness.passBothPriorities();
    }
}
