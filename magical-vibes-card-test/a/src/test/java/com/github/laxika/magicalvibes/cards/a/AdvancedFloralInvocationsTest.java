package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AdvancedFloralInvocations.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class AdvancedFloralInvocationsTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall mills two cards")
    void landfallMillsTwoCards() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new Forest();
        harness.addToBattlefield(player1, new AdvancedFloralInvocations());
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Level two perpetually boosts creature cards in the graveyard")
    void levelTwoBoostsCreatureCardsInGraveyard() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        levelUpToTwo(classPermanent);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        levelUpFromTwoToThree(classPermanent);

        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareForSorcery();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(3);
    }

    @Test
    @DisplayName("Level three allows playing lands and casting creature spells from the graveyard")
    void levelThreeAllowsPlayingLandsAndCastingCreaturesFromGraveyard() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));
        levelUpToThree(classPermanent);
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareForSorcery();

        harness.playGraveyardLand(player1, 0);
        resolveAllTriggers();
        prepareForSorcery();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void levelTwoLandfallCreatesTwoSeparateTriggeredAbilities() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        levelUpToTwo(classPermanent);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
    }

    @Test
    void opponentsLandDoesNotTriggerEitherLandfallAbility() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        levelUpToTwo(classPermanent);
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).first().isSameAs(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void levelOneLandfallDoesNotPerpetuallyBoostCreatures() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();
        levelUpToThree(classPermanent);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(2);
    }

    @Test
    void creatureCastFromGraveyardStillEntersWithFinalityAfterClassIsDestroyed() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        levelUpToThree(classPermanent);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromGraveyard(player1, 0);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, classPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Advanced Floral Invocations");
        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        entered.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void levelTwoLandfallBoostResolvesAfterClassIsDestroyed() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        levelUpToTwo(classPermanent);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, classPermanent.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent replacement = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        levelUpToThree(replacement);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(3);
    }

    @Test
    void graveyardPermissionsEndWhenClassLeavesBattlefield() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));
        levelUpToThree(classPermanent);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, classPermanent.getId());
        harness.passBothPriorities();
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, bears);
    }

    @Test
    void levelThreeDoesNotAllowCastingNoncreatureSpellsFromGraveyard() {
        Permanent classPermanent = harness.addToBattlefieldAndReturn(player1,
                new AdvancedFloralInvocations());
        Naturalize naturalize = new Naturalize();
        harness.setGraveyard(player1, List.of(naturalize));
        levelUpToThree(classPermanent);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(naturalize);
    }

    private void levelUpToTwo(Permanent classPermanent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, battlefieldIndex(classPermanent), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent classPermanent) {
        levelUpToTwo(classPermanent);
        levelUpFromTwoToThree(classPermanent);
    }

    private void levelUpFromTwoToThree(Permanent classPermanent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, battlefieldIndex(classPermanent), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
