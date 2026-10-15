package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AragornTheUniter;
import com.github.laxika.magicalvibes.cards.b.BattleScarredGoblin;
import com.github.laxika.magicalvibes.cards.f.FloweringOfTheWhiteTree;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinasTirith.class, AragornTheUniter.class, BattleScarredGoblin.class, FloweringOfTheWhiteTree.class})
class MinasTirithTest extends BaseCardTest {

    @Test
    void entersTappedWithoutLegendaryCreature() {
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new AragornTheUniter());
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isFalse();
    }

    @Test
    void tapsForWhiteMana() {
        Permanent minasTirith = addReadyMinasTirith();

        harness.activateAbility(player1, indexOf(minasTirith), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(minasTirith.isTapped()).isTrue();
    }

    @Test
    void drawsAfterAttackingWithTwoCreatures() {
        Permanent minasTirith = addReadyMinasTirith();
        Card drawn = new BattleScarredGoblin();
        harness.setLibrary(player1, List.of(drawn));
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(minasTirith), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(minasTirith.isTapped()).isTrue();
    }

    @Test
    void cannotDrawWithoutAttackingWithTwoCreatures() {
        Permanent minasTirith = addReadyMinasTirith();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked with two or more creatures");
        assertThat(minasTirith.isTapped()).isFalse();
    }

    @Test
    void entersTappedWithOnlyNonlegendaryCreature() {
        harness.addToBattlefield(player1, new BattleScarredGoblin());
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isTrue();
    }

    @Test
    void entersTappedWithOpponentsLegendaryCreature() {
        harness.addToBattlefield(player2, new AragornTheUniter());
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isTrue();
    }

    @Test
    void entersTappedWithOnlyLegendaryNoncreature() {
        harness.addToBattlefield(player1, new FloweringOfTheWhiteTree());
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isTrue();
    }

    @Test
    void cannotDrawAfterOnlyOneCreatureAttacked() {
        Permanent minasTirith = addReadyMinasTirith();
        Permanent attacker = addCreatureReady(player1, new BattleScarredGoblin());
        declareAttackers(List.of(indexOf(attacker)));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked with two or more creatures");
        assertThat(minasTirith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    void opponentsAttacksDoNotPermitDrawing() {
        Permanent minasTirith = addReadyMinasTirith();
        gd.creaturesAttackedCountThisTurn.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked with two or more creatures");
        assertThat(minasTirith.isTapped()).isFalse();
    }

    @Test
    void drawsAfterTwoCreaturesAreDeclaredAsAttackers() {
        Permanent minasTirith = addReadyMinasTirith();
        Permanent first = addCreatureReady(player1, new BattleScarredGoblin());
        Permanent second = addCreatureReady(player1, new BattleScarredGoblin());
        declareAttackers(List.of(indexOf(first), indexOf(second)));
        Card drawn = new BattleScarredGoblin();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(minasTirith), 1, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(minasTirith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void sameCreatureAttackingInTwoCombatsDoesNotPermitDrawing() {
        Permanent minasTirith = addReadyMinasTirith();
        Permanent attacker = addCreatureReady(player1, new BattleScarredGoblin());
        declareAttackers(List.of(indexOf(attacker)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        attacker.untap();
        declareAttackers(List.of(indexOf(attacker)));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked with two or more creatures");
        assertThat(minasTirith.isTapped()).isFalse();
    }

    @Test
    void distinctCreaturesAttackingInSeparateCombatsPermitDrawing() {
        Permanent minasTirith = addReadyMinasTirith();
        Permanent first = addCreatureReady(player1, new BattleScarredGoblin());
        Permanent second = addCreatureReady(player1, new BattleScarredGoblin());
        declareAttackers(List.of(indexOf(first)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        declareAttackers(List.of(indexOf(second)));
        Card drawn = new BattleScarredGoblin();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(minasTirith), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void drawAbilityRequiresWhiteMana() {
        Permanent minasTirith = addReadyMinasTirith();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(minasTirith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void tappedLandCannotActivateDrawAbility() {
        Permanent minasTirith = addReadyMinasTirith();
        minasTirith.tap();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MinasTirith()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMinasTirith() {
        return harness.addToBattlefieldAndReturn(player1, new MinasTirith());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
