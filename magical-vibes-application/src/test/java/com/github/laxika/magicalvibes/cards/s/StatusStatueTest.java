package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusStatueTest extends BaseCardTest {

    private static final int STATUS = 0;
    private static final int STATUE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Status boosts a creature and grants deathtouch until end of turn")
    void statusBoostsAndGrantsDeathtouch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StatusStatue()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, STATUS, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Statue destroys artifacts, creatures, and enchantments")
    void statueDestroysEachAllowedPermanentType() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new StatusStatue(), new StatusStatue(), new StatusStatue()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalInstant(player1, 0, STATUE, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.castModalInstant(player1, 0, STATUE, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.castModalInstant(player1, 0, STATUE, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Statue cannot target a land")
    void statueCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StatusStatue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, STATUE, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Fuse applies Status before destroying the Statue target")
    void fuseResolvesBothHalves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StatusStatue()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, FUSE, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Fuse allows the same creature as both targets")
    void fuseAllowsSharedCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StatusStatue()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, FUSE, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
