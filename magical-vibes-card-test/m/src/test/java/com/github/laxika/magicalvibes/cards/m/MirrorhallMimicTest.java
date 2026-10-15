package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BallistaWatcher;
import com.github.laxika.magicalvibes.cards.b.BallistaWielder;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GhastlyMimicry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
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

@CardUsed({MirrorhallMimic.class, GhastlyMimicry.class, GrizzlyBears.class, FountainOfYouth.class,
        BallistaWatcher.class, BallistaWielder.class})
class MirrorhallMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a copy of a creature and adds Spirit")
    void entersAsCopyWithSpiritSubtype() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent mimic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof MirrorhallMimic)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);
        assertThat(mimic.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Declining the copy leaves a 0/0 that dies")
    void decliningCopyDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() instanceof MirrorhallMimic);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MirrorhallMimic);
    }

    @Test
    @DisplayName("Disturb enters transformed, then creates a Spirit token copy at upkeep")
    void disturbCreatesSpiritTokenCopyAtUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, bears.getId());

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof MirrorhallMimic)
                .findFirst()
                .orElseThrow();
        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(GhastlyMimicry.class);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The transformed Aura is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGoingToGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MirrorhallMimic card = new MirrorhallMimic();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveFlashback(player1, 0, bears.getId());
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof MirrorhallMimic)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(card.getId());
    }

    @Test
    void noCreatureToCopyDiesWithoutACopyChoice() {
        harness.setHand(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mirrorhall Mimic");
        harness.assertInGraveyard(player1, "Mirrorhall Mimic");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyDoesNotCopyCountersOrTappedState() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.tap();
        harness.setHand(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent mimic = findPermanent(player1, "Grizzly Bears");
        assertThat(mimic.isTapped()).isFalse();
        assertThat(mimic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);
        assertThat(mimic.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
    }

    @Test
    void disturbIsExiledWhenItsTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MirrorhallMimic card = new MirrorhallMimic();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castFlashback(player1, 0, bears.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(card.getId());
    }

    @Test
    void enchantingAnOpponentCreatureCreatesTokensOnlyOnAurasControllersUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveFlashback(player1, 0, bears.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().isToken()))
                .singleElement().satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void upkeepTriggerStillCreatesTokenAfterAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveFlashback(player1, 0, bears.getId());
        Permanent aura = findPermanent(player1, "Ghastly Mimicry");

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(p -> p.getCard().isToken()))
                .singleElement().satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SPIRIT);
                });
    }

    @Test
    void tokenCopyOfDoubleFacedCreatureTransformsAndRetainsSpirit() {
        gd.dayNight = DayNight.DAY;
        Permanent watcher = harness.addToBattlefieldAndReturn(player2, new BallistaWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MirrorhallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveFlashback(player1, 0, watcher.getId());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);

        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(token.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
    }
}
