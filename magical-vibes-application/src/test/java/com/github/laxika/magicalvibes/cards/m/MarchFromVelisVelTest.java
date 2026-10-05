package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchFromVelisVel.class, Desert.class, Forest.class, GrizzlyBears.class, Clone.class})
class MarchFromVelisVelTest extends BaseCardTest {

    @Test
    void copiesControlledDesertsWithHasteAndLeavesOtherLandsAndOpponentsLandsAlone() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentDesert = harness.addToBattlefieldAndReturn(player2, new Desert());

        castAndChooseDesert(target);

        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(desert.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(desert.getCard().getPower()).isEqualTo(2);
        assertThat(desert.getCard().getToughness()).isEqualTo(2);
        assertThat(forest.getCard().getName()).isEqualTo("Forest");
        assertThat(opponentDesert.getCard().getName()).isEqualTo("Desert");
    }

    @Test
    void copiesRevertAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());

        castAndChooseDesert(target);
        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(desert.getCard().getName()).isEqualTo("Desert");
        assertThat(desert.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void flashbackCopiesTheChosenLandsAndExilesTheSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        harness.setGraveyard(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, target.getId());
        harness.handleListChoice(player1, "DESERT");

        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "March from Velis Vel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("March from Velis Vel"));
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .hasMessageContaining("creature you control");
    }

    @Test
    void laterCopiesDoNotInheritTheSeparatelyGrantedHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());

        castAndChooseDesert(target);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, desert.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElseThrow();
        assertThat(clone.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(clone.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(desert.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void copyingPreservesLandCountersAndTappedStatusWithoutCopyingTargetCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        desert.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        desert.tap();

        castAndChooseDesert(target);

        assertThat(desert.isTapped()).isTrue();
        assertThat(desert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(desert.getCard().getPower()).isEqualTo(2);
        assertThat(desert.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void choosingATypeWithoutMatchingLandsDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAndChooseDesert(target);

        assertThat(forest.getCard().getName()).isEqualTo("Forest");
        assertThat(target.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
        harness.assertInGraveyard(player1, "March from Velis Vel");
    }

    @Test
    void losingTheOnlyTargetPreventsAnyLandFromBecomingACopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        harness.setHand(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(desert.getCard().getName()).isEqualTo("Desert");
        harness.assertInGraveyard(player1, "March from Velis Vel");
    }

    private void castAndChooseDesert(Permanent target) {
        harness.setHand(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "DESERT");
    }
}
