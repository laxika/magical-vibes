package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivinePurge.class, GrizzlyBears.class, Memnite.class, HillGiant.class,
        HowlingMine.class, RodOfRuin.class, GloriousAnthem.class, ScatheZombies.class, Unsummon.class})
class DivinePurgeTest extends BaseCardTest {

    @Test
    void exilesSmallArtifactsAndCreaturesButNotLargerCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.addToBattlefield(player2, new HillGiant());

        castPurge();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(memnite.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void exiledPermanentCostsTwoMoreAndEntersTapped() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castPurge();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void exilesNoncreatureArtifactsButLeavesLargeArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castPurge();

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    void artifactCreatureReceivesOnlyOneCostIncrease() {
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        castPurge();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, memnite.getOriginalCard().getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void repeatedPurgesAccumulateCostIncreasesWithoutChangingManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castPurge();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        castPurge();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void exilesCreaturesAtManaValueThree() {
        Permanent zombies = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        castPurge();

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.findExiledCard(zombies.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void perpetualPenaltiesPersistWhenReturnedToHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castPurge();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertInHand(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    private void castPurge() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new DivinePurge(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
