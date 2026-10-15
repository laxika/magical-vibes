package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({RallyTheMonastery.class, CrawWurm.class, GrizzlyBears.class, Shock.class, Afflict.class})
class RallyTheMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Token mode creates two Monk tokens with prowess")
    void createsMonksWithProwess() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> monks = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(monks).hasSize(2);
        for (Permanent monk : monks) {
            assertThat(monk.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(monk.getCard().getSubtypes()).containsExactly(CardSubtype.MONK);
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        }

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The pump mode gives up to two controlled creatures +2/+2")
    void pumpsUpToTwoControlledCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The destroy mode destroys a creature with power 4 or greater")
    void destroysLargeCreature() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("The destroy mode rejects a creature below power 4")
    void rejectsSmallCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("The spell costs two less after casting another spell this turn")
    void reducedCostAfterAnotherSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("The spell still needs its full cost without another spell")
    void needsFullCostWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("The pump mode can be cast with no targets")
    void pumpModeAllowsZeroTargets() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rally the Monastery");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The pump mode can target one creature and expires at end of turn")
    void pumpsOneCreatureUntilEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The pump mode rejects an opponent's creature")
    void pumpModeRejectsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The pump mode rejects more than two targets")
    void pumpModeRejectsThreeTargets() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The pump still affects its remaining target if the other dies in response")
    void pumpResolvesForRemainingTarget() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        harness.setHand(player2, List.of(new Shock()));
        addFullMana();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another spell need not resolve to enable the cost reduction")
    void reducedCostWhileAnotherSpellIsOnStack() {
        harness.setHand(player1, List.of(new Shock(), new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monk")).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's spell does not enable the cost reduction")
    void opponentsSpellDoesNotReduceCost() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Monk prowess ignores creature spells and opponents' spells")
    void monksIgnoreCreatureAndOpponentSpells() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        List<Permanent> monks = findPermanents(player1, "Monk");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(monks).hasSize(2);
        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Prowess resolves before the triggering spell and expires at end of turn")
    void monkProwessResolvesBeforeSpellAndExpires() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        List<Permanent> monks = findPermanents(player1, "Monk");
        Permanent target = monks.getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        }
    }


    @Test
    @DisplayName("The destroy mode uses effective power and can destroy your own power-four creature")
    void destroysOwnCreatureWithExactlyFourEffectivePower() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery(), new RallyTheMonastery()));
        addFullMana();
        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroy mode does not destroy a target whose power falls below four in response")
    void destroyRechecksEffectivePowerOnResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery(), new RallyTheMonastery()));
        addFullMana();
        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));

        harness.setHand(player2, List.of(new Afflict()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rally the Monastery");
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
