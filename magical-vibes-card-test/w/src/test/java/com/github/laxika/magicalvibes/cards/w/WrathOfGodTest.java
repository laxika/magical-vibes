package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathOfGod.class, GrizzlyBears.class, HowlingMine.class, DrudgeSkeletons.class})
class WrathOfGodTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents untouched")
    void destroysAllCreaturesAndLeavesNoncreaturePermanentsUntouched() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());

        castWrathOfGod();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Wrath of God");
    }

    @Test
    @DisplayName("Creatures cannot regenerate from Wrath of God")
    void creaturesCannotRegenerate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castWrathOfGod();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible creatures survive Wrath of God")
    void indestructibleCreaturesSurvive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castWrathOfGod();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves without creatures on the battlefield")
    void resolvesWithoutCreatures() {
        castWrathOfGod();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wrath of God");
    }

    @Test
    @DisplayName("Regeneration activated in response cannot save a creature")
    void regenerationInResponseCannotSaveCreature() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Drudge Skeletons");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Wrath of God");
    }

    @Test
    @DisplayName("Shroud does not protect creatures from a spell that does not target")
    void destroysCreaturesWithShroud() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.getGrantedKeywords().add(Keyword.SHROUD);

        castWrathOfGod();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castWrathOfGod() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
