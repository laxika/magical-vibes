package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarigaazsWhelp.class, FurnaceWhelp.class, GrizzlyBears.class,
        LurkerInTheDeep.class, PsychogenicProbe.class})
class DarigaazsWhelpTest extends BaseCardTest {

    @Test
    void drawingDragonPerpetuallyBoostsIt() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new DarigaazsWhelp());
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));

        draw(player1.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent drawnDragon = findPermanent(player1, dragon);
        assertThat(gqs.getEffectivePower(gd, drawnDragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drawnDragon)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
    }

    @Test
    void kickedEntryBoostsTheWhelpAndSeeksAndBoostsADragon() {
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), dragon));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent whelp = findPermanent(player1, "Darigaaz's Whelp");
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);

        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent foundDragon = findPermanent(player1, dragon);
        assertThat(gqs.getEffectivePower(gd, foundDragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, foundDragon)).isEqualTo(3);
    }

    @Test
    void drawingNonDragonDoesNotBoostIt() {
        harness.addToBattlefield(player1, new DarigaazsWhelp());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unkickedEntryDoesNotSeekOrBoost() {
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent whelp = findPermanent(player1, "Darigaaz's Whelp");
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedEntryStillBoostsWhelpWhenNoDragonCanBeSought() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent whelp = findPermanent(player1, "Darigaaz's Whelp");
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void opponentDrawingDragonDoesNotTriggerWhelp() {
        harness.addToBattlefield(player1, new DarigaazsWhelp());
        harness.setLibrary(player2, List.of(new FurnaceWhelp()));

        draw(player2.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoWhelpsEachBoostTheSameDrawnDragon() {
        harness.addToBattlefield(player1, new DarigaazsWhelp());
        harness.addToBattlefield(player1, new DarigaazsWhelp());
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));

        draw(player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(dragon));
        harness.passBothPriorities();

        Permanent drawnDragon = findPermanent(player1, dragon);
        assertThat(gqs.getEffectivePower(gd, drawnDragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drawnDragon)).isEqualTo(4);
    }

    @Test
    void seekingDoesNotCauseShuffleTriggers() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void seekingDragonTriggersAbilitiesThatWatchSeeking() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new LurkerInTheDeep());
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isFaceDown).hasSize(1);
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

}
