package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BindingGrasp;
import com.github.laxika.magicalvibes.cards.f.FyndhornBrownie;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeriekeRiBerit.class, BalduvianBears.class, Incinerate.class, FyndhornBrownie.class,
        SnowCoveredForest.class, BindingGrasp.class, IcyManipulator.class})
class MeriekeRiBeritTest extends BaseCardTest {

    @Test
    @DisplayName("Merieke doesn't untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent merieke = addReadyMerieke(player1);
        merieke.tap();

        harness.performUntapStep(player1);

        assertThat(merieke.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T} gains control of target creature for as long as its controller controls Merieke")
    void gainsControlOfTargetCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        activateSteal(merieke, bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(merieke.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(bears.getId()).sourcePermanentId()).isEqualTo(merieke.getId());
    }

    @Test
    @DisplayName("Merieke leaving the battlefield destroys the stolen creature")
    void leavingDestroysStolenCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        activateSteal(merieke, bears);

        // Incinerate Merieke: she dies, the leaves-the-battlefield trigger destroys the stolen creature.
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities(); // Incinerate resolves, Merieke dies, trigger goes on the stack.
        harness.passBothPriorities(); // Trigger resolves.

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("Merieke becoming untapped destroys the stolen creature")
    void untappingDestroysStolenCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());

        activateSteal(merieke, bears);

        addBrownieMana(player1);
        untapWithBrownie(brownie, merieke);
        harness.passBothPriorities(); // Trigger resolves.

        assertThat(merieke.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("A second untap after the stolen creature is gone destroys nothing")
    void secondUntapDestroysNothing() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        Permanent other = addCreatureReady(player2, new BalduvianBears());
        Permanent firstBrownie = addCreatureReady(player1, new FyndhornBrownie());
        Permanent secondBrownie = addCreatureReady(player1, new FyndhornBrownie());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());

        activateSteal(merieke, bears);

        addBrownieMana(player1);
        untapWithBrownie(firstBrownie, merieke);
        harness.passBothPriorities(); // First trigger resolves.

        // Tap then untap Merieke again: the link is spent, so player2's other creature survives.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int icyManipulatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(icyManipulator);
        harness.activateAbility(player1, icyManipulatorIndex, null, merieke.getId());
        harness.passBothPriorities();

        addBrownieMana(player1);
        untapWithBrownie(secondBrownie, merieke);
        harness.passBothPriorities(); // Second trigger resolves.

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(other.getId()));
    }

    @Test
    @DisplayName("Each resolved activation remembers its own creature until its delayed trigger resolves")
    void delayedDestructionKeepsTheActivationLink() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent first = addCreatureReady(player2, new BalduvianBears());
        Permanent second = addCreatureReady(player2, new BalduvianBears());
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());

        activateSteal(merieke, first);

        // Untapping Merieke puts the first activation's destruction trigger on the stack.
        addBrownieMana(player1);
        untapWithBrownie(brownie, merieke);

        // A new activation resolves above that trigger and must create an independent link.
        int meriekeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(merieke);
        harness.activateAbility(player1, meriekeIndex, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(first.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(second.getCard().getId()));
    }

    @Test
    @DisplayName("Merieke can target only a creature")
    void onlyTargetsCreatures() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        int meriekeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(merieke);

        assertThatThrownBy(() -> harness.activateAbility(player1, meriekeIndex, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Losing control of Merieke returns the stolen creature without destroying it")
    void losingControlReturnsStolenCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        activateSteal(merieke, bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BindingGrasp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, merieke.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(merieke.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    private Permanent addReadyMerieke(Player player) {
        return addCreatureReady(player, new MeriekeRiBerit());
    }

    private void activateSteal(Permanent merieke, Permanent target) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(merieke);
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();
    }

    private void addBrownieMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void untapWithBrownie(Permanent brownie, Permanent target) {
        int brownieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brownie);
        harness.activateAbility(player1, brownieIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
