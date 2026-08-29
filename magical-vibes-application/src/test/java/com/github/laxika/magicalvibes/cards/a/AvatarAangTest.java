package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EarthbendingLesson;
import com.github.laxika.magicalvibes.cards.f.FireSages;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterbendingLesson;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AvatarAang.class,
        AangMasterOfElements.class,
        WaterbendingLesson.class,
        EarthbendingLesson.class,
        FireSages.class,
        AangTheLastAirbender.class,
        GrizzlyBears.class,
        Forest.class
})
class AvatarAangTest extends BaseCardTest {

    @Test
    @DisplayName("Avatar Aang draws when its controller waterbends")
    void drawsWhenControllerWaterbends() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AvatarAang());
        Permanent firstSource = addReadyCreature(player1);
        Permanent secondSource = addReadyCreature(player1);
        WaterbendingLesson lesson = new WaterbendingLesson();
        harness.setHand(player1, List.of(lesson));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMana(player1, ManaColor.BLUE, 1);
        addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false,
                null, null, null,
                List.of(firstSource.getId(), secondSource.getId()), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);
        resolveAllTriggers();

        assertThat(firstSource.isTapped()).isTrue();
        assertThat(secondSource.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(aang.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Avatar Aang transforms after its controller completes all four bends")
    void transformsAfterAllFourBends() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AvatarAang());
        Permanent firstSource = addReadyCreature(player1);
        Permanent secondSource = addReadyCreature(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        FireSages fireSages = addReadyCreature(player1, new FireSages());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new WaterbendingLesson()));
        addMana(player1, ManaColor.BLUE, 1);
        addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false,
                null, null, null,
                List.of(firstSource.getId(), secondSource.getId()), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new EarthbendingLesson()));
        addMana(player1, ManaColor.GREEN, 1);
        addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, forest.getId());
        resolveAllTriggers();

        declareAttacker(fireSages);
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        Permanent airbendTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangTheLastAirbender()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, airbendTarget.getId());
        resolveAllTriggers();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangMasterOfElements.class);
    }

    @Test
    @DisplayName("Aang, Master of Elements transforms back for its upkeep rewards")
    void backFaceUpkeepAbilityTransformsBackAndRewards() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AvatarAang());
        aang.setCard(aang.getOriginalCard().getBackFaceCard());
        aang.setTransformed(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(aang.isTransformed()).isFalse();
        assertThat(aang.getCard()).isInstanceOf(AvatarAang.class);
        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana(player1, ManaColor.GREEN, 1);
        addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }

    private void declareAttacker(Permanent attacker) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }
}
