package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TailSlash;
import com.github.laxika.magicalvibes.cards.z.ZurgoBellstriker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardenedBerserker.class, GrizzlyBears.class, DragonFodder.class, ZurgoBellstriker.class,
        TailSlash.class})
class HardenedBerserkerTest extends BaseCardTest {

    @Test
    void attackingReducesTheNextSpellByOneGenericMana() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    void reductionIsConsumedByOnlyTheNextSpell() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void reductionAppliesToNoncreatureSpells() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Dragon Fodder");
    }

    @Test
    void twoAttackTriggersReduceTheSameNextSpellByTwo() {
        addCreatureReady(player1, new HardenedBerserker());
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0, 1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new HardenedBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hardened Berserker")).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void coloredOnlySpellStillConsumesTheReduction() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new ZurgoBellstriker(), new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Zurgo Bellstriker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void reductionAppliesToDashAlternativeCost() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Zurgo Bellstriker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void unusedReductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void opponentSpellDoesNotUseReductionAndRemovingSourceDoesNotEndIt() {
        var berserker = addCreatureReady(player1, new HardenedBerserker());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        var opposingBerserker = addCreatureReady(player2, new HardenedBerserker());

        harness.setHand(player1, List.of(new DragonFodder()));
        harness.setHand(player2, List.of(new TailSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0,
                List.of(opposingBerserker.getId(), berserker.getId()));

        harness.assertInGraveyard(player1, "Hardened Berserker");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Dragon Fodder");
    }
}
