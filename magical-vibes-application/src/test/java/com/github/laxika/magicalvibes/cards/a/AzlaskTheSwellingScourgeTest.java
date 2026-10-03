package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EyelessWatcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozileksPredator;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzlaskTheSwellingScourge.class, EyelessWatcher.class, KozileksPredator.class,
        GrizzlyBears.class, Ornithopter.class, PaintersServant.class, MaskwoodNexus.class})
class AzlaskTheSwellingScourgeTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenAControlledColorlessCreatureDies() {
        addCreatureReady(player1, new AzlaskTheSwellingScourge());
        Permanent coloredCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownColorlessCreature = addCreatureReady(player1, new Ornithopter());
        Permanent opposingColorlessCreature = addCreatureReady(player2, new Ornithopter());

        putIntoGraveyard(coloredCreature);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());

        putIntoGraveyard(opposingColorlessCreature);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());

        putIntoGraveyard(ownColorlessCreature);
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void gainsExperienceWhenAzlaskDies() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());

        putIntoGraveyard(azlask);

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void boostsAllCreaturesAndGrantsTheAbilitiesToScionsAndSpawns() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        harness.enterBattlefieldAndReturn(player1, new EyelessWatcher());
        harness.enterBattlefieldAndReturn(player1, new KozileksPredator());
        resolveAllTriggers();
        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        List<Permanent> spawns = findPermanents(player1, "Eldrazi Spawn");
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(azlask), 0,
                null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, azlask)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, azlask)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(scions).allMatch(scion -> gqs.getEffectivePower(gd, scion) == 3
                && gqs.hasKeyword(gd, scion, Keyword.INDESTRUCTIBLE));
        assertThat(spawns).allMatch(spawn -> gqs.getEffectivePower(gd, spawn) == 2
                && gqs.hasKeyword(gd, spawn, Keyword.INDESTRUCTIBLE));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();

        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        scions.getFirst().setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scions.getFirst())));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defendingCreature);
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }

    @Test
    void doesNotGainExperienceForACreatureMadeColoredByPaintersServant() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        harness.castFromHand(player1, new PaintersServant(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.getEffectiveColors(gd, thopter)).contains(CardColor.BLUE);

        putIntoGraveyard(thopter);

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();

        putIntoGraveyard(azlask);

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void determinesExperienceAtResolutionAndDoesNotBoostLaterArrivals() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        gd.playerExperienceCounters.put(player1.getId(), 1);
        activateAzlask(azlask);

        putIntoGraveyard(thopter);

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(gqs.getEffectivePower(gd, azlask)).isEqualTo(4);
        Permanent lateThopter = addCreatureReady(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, lateThopter)).isZero();

        putIntoGraveyard(lateThopter);

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 3);
        assertThat(gqs.getEffectivePower(gd, azlask)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, azlask)).isEqualTo(4);
    }

    @Test
    void grantsSpawnAbilitiesWithNoExperienceAndTheyExpireAtEndOfTurn() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        harness.enterBattlefieldAndReturn(player1, new KozileksPredator());
        resolveAllTriggers();
        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        activateAzlask(azlask);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.hasKeyword(gd, spawn, Keyword.INDESTRUCTIBLE)).isTrue();
        spawn.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spawn)));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defender);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, spawn, Keyword.INDESTRUCTIBLE)).isFalse();
        Permanent nextDefender = addCreatureReady(player2, new GrizzlyBears());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spawn)));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nextDefender);
    }

    @Test
    void repeatedActivationsGrantSeparateAnnihilatorTriggers() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        harness.enterBattlefieldAndReturn(player1, new KozileksPredator());
        resolveAllTriggers();
        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        gd.playerExperienceCounters.put(player1.getId(), 1);
        activateAzlask(azlask);
        harness.passBothPriorities();
        activateAzlask(azlask);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(3);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondDefender = addCreatureReady(player2, new GrizzlyBears());
        spawn.setSummoningSick(false);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spawn)));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(defender.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defender);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondDefender);
    }

    private void activateAzlask(Permanent azlask) {
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(azlask), 0,
                null, null);
    }

    @Test
    void grantsIndestructibleToAzlaskWhenItIsAScionAndSpawn() {
        Permanent azlask = addCreatureReady(player1, new AzlaskTheSwellingScourge());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        activateAzlask(azlask);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, azlask, Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(azlask)));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defender);
    }
}
