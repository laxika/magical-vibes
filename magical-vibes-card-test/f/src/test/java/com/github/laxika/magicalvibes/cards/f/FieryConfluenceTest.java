package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.u.UrzasArmor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryConfluence.class, GiantSpider.class, GrizzlyBears.class, Spellbook.class,
        Shatter.class, UrzasArmor.class})
class FieryConfluenceTest extends BaseCardTest {

    @Test
    void repeatedCreatureModeDealsOneDamageThreeTimes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void repeatedOpponentModeDealsTwoDamageThreeTimes() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{1, 1, 1}, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void repeatedArtifactModeDestroysThreeTargetArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        cast(new int[]{2, 2, 2}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Spellbook"))
                .hasSize(3);
    }

    @Test
    void allThreeModesResolveInPrintedOrder() {
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setLife(player2, 20);

        cast(new int[]{0, 1, 2}, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    void artifactModeRejectsNonArtifactTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        assertThatThrownBy(() -> cast(new int[]{2, 2, 2},
                List.of(creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modesResolveInPrintedOrderEvenWhenSelectedInReverseOrder() {
        Permanent armor = harness.addToBattlefieldAndReturn(player2, new UrzasArmor());
        harness.setLife(player2, 20);

        cast(new int[]{2, 1, 1}, List.of(armor.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Urza's Armor");
        harness.assertInGraveyard(player2, "Urza's Armor");
    }

    @Test
    void repeatedDamageModesAreSeparateDamageEvents() {
        harness.addToBattlefield(player2, new UrzasArmor());
        harness.setLife(player2, 20);

        cast(new int[]{1, 1, 1}, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void repeatedArtifactModeMayTargetTheSameArtifactEachTime() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        cast(new int[]{2, 2, 2}, List.of(artifact.getId(), artifact.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Spellbook"))
                .hasSize(1);
    }

    @Test
    void artifactModeMayDestroyAnArtifactControlledByTheCaster() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLife(player2, 20);

        cast(new int[]{1, 1, 2}, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertLife(player2, 16);
    }

    @Test
    void noModesResolveWhenTheOnlyTargetBecomesIllegal() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        cast(new int[]{0, 1, 2}, List.of(artifact.getId()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .allSatisfy(permanent -> assertThat(permanent.getMarkedDamage()).isZero());
        harness.assertInGraveyard(player1, "Fiery Confluence");
    }

    @Test
    void remainingModesResolveWhenOneOfTwoArtifactTargetsBecomesIllegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setLife(player2, 20);

        cast(new int[]{1, 2, 2}, List.of(first.getId(), second.getId()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Spellbook"))
                .hasSize(2);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new FieryConfluence()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                targetIds);
    }
}
