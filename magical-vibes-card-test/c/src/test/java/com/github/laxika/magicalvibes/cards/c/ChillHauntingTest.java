package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArkOfBlight;
import com.github.laxika.magicalvibes.cards.t.TwistedAbomination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillHaunting.class, TwistedAbomination.class, ClutchOfUndeath.class, ArkOfBlight.class})
class ChillHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling creature cards sets X and gives the target creature -X/-X")
    void debuffsTargetByNumberOfExiledCreatureCards() {
        harness.setGraveyard(player1, List.of(new TwistedAbomination(), new ClutchOfUndeath()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .allMatch(ClutchOfUndeath.class::isInstance);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling enough creature cards gives a lethal -X/-X")
    void lethalDebuffDestroysTarget() {
        harness.setGraveyard(player1, List.of(
                new TwistedAbomination(), new TwistedAbomination(), new TwistedAbomination()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Twisted Abomination");
    }

    @Test
    @DisplayName("Exiling zero creature cards leaves the target unchanged")
    void zeroExilesLeavesTargetUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new TwistedAbomination()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only creature cards can be exiled for the additional cost")
    void cannotExileNonCreatureCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setGraveyard(player1, List.of(new ClutchOfUndeath()));
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must exile creature cards");
    }

    @Test
    @DisplayName("The spell cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArkOfBlight());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, artifact.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The same graveyard card cannot be selected twice for the additional cost")
    void cannotExileDuplicateGraveyardCardIndex() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setGraveyard(player1, List.of(new TwistedAbomination()));
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate graveyard card indices");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .allMatch(TwistedAbomination.class::isInstance);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell does nothing if its target is no longer on the battlefield")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.setGraveyard(player1, List.of(new TwistedAbomination()));
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unsorted creature selections exile only the selected cards and fix X at casting")
    void mixedGraveyardSelectionsFixXAtCasting() {
        TwistedAbomination first = new TwistedAbomination();
        ClutchOfUndeath aura = new ClutchOfUndeath();
        TwistedAbomination second = new TwistedAbomination();
        harness.setGraveyard(player1, List.of(first, aura, second));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(2, 0));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.setGraveyard(player1, List.of(new TwistedAbomination()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration cannot save a creature reduced to zero toughness")
    void zeroToughnessIgnoresRegenerationShield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TwistedAbomination());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        harness.setGraveyard(player1, List.of(
                new TwistedAbomination(), new TwistedAbomination(), new TwistedAbomination()));
        harness.setHand(player1, List.of(new ChillHaunting()));

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Twisted Abomination");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }
}
