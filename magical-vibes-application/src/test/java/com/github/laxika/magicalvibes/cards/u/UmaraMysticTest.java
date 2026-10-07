package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraMystic.class, Shock.class, Divination.class, FugitiveWizard.class, GrizzlyBears.class})
class UmaraMysticTest extends BaseCardTest {

    @Test
    void instantAndSorcerySpellsBoostSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(mystic.getPowerModifier()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(mystic.getPowerModifier()).isEqualTo(4);
    }

    @Test
    void wizardSpellBoostsSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FugitiveWizard()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void nonMatchingCreatureSpellDoesNotTrigger() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(mystic.getPowerModifier()).isEqualTo(2);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void opponentsMatchingSpellDoesNotTrigger() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new UmaraMystic()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isZero();
        assertThat(mystic.getToughnessModifier()).isZero();
    }

    @Test
    void controllerInstantTriggersDuringOpponentsTurn() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.getToughnessModifier()).isZero();
    }

    @Test
    void castingMysticBoostsExistingMysticsButNotTheNewOne() {
        Permanent first = addMystic(player1);
        Permanent second = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new UmaraMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent != first && permanent != second)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getPowerModifier()).isZero();
                    assertThat(permanent.getToughnessModifier()).isZero();
                });
    }

    @Test
    void mysticDoesNotTriggerFromItsOwnCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new UmaraMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getPowerModifier()).isZero();
                    assertThat(permanent.getToughnessModifier()).isZero();
                });
    }

    private Permanent addMystic(Player player) {
        return addCreatureReady(player, new UmaraMystic());
    }
}
