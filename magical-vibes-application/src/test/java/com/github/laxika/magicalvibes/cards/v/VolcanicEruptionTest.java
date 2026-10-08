package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicEruption.class, Forest.class, GrizzlyBears.class, HillGiant.class, Mountain.class,
        PhantasmalTerrain.class})
class VolcanicEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two Mountains and deals 2 damage to each creature and each player")
    void destroysMountainsAndBlasts() {
        Permanent m1 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent m2 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 dies to 2 damage
        harness.addToBattlefield(player1, new HillGiant());     // 3/3 survives 2 damage
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2: {2}{U}{U}{U}

        harness.castSorcery(player1, 0, 2, List.of(m1.getId(), m2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player1, 18); // caster is also "each player"
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Requires exactly X Mountain targets")
    void requiresExactlyXTargets() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage equals only the Mountains actually put into a graveyard this way")
    void damageCountsActuallyDestroyed() {
        Permanent m1 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent m2 = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2

        harness.castSorcery(player1, 0, 2, List.of(m1.getId(), m2.getId()));

        // One targeted Mountain leaves before resolution — only one is put into a graveyard,
        // so the blast deals 1 damage, not 2.
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(m2.getId()));

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("X=0 destroys nothing and deals no damage")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=0: {U}{U}{U}

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a non-Mountain permanent")
    void cannotTargetNonMountain() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountains");
    }

    @Test
    void regeneratedMountainDoesNotIncreaseDamage() {
        Permanent protectedMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        protectedMountain.setRegenerationShield(1);
        Permanent otherMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 2, List.of(protectedMountain.getId(), otherMountain.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void mountainThatChangesTypeBeforeResolutionIsUnaffected() {
        Permanent changedMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent otherMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 2, List.of(changedMountain.getId(), otherMountain.getId()));

        Permanent terrain = harness.addToBattlefieldAndReturn(player2, new PhantasmalTerrain());
        terrain.setAttachedTo(changedMountain.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(changedMountain).doesNotContain(otherMountain);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void allTargetsIllegalPreventsDamage() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 1, List.of(mountain.getId()));

        Permanent terrain = harness.addToBattlefieldAndReturn(player2, new PhantasmalTerrain());
        terrain.setAttachedTo(mountain.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Volcanic Eruption");
    }

    @Test
    void xCanExceedOneHundred() {
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new Mountain()).getId());
        }
        harness.setLife(player1, 200);
        harness.setLife(player2, 200);
        harness.setHand(player1, List.of(new VolcanicEruption()));
        harness.addMana(player1, ManaColor.BLUE, 104);

        harness.castSorcery(player1, 0, 101, targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 99);
        harness.assertLife(player2, 99);
    }
}
