package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DauntlessOnslaught;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaxAndCymede.class, GrizzlyBears.class, Shock.class, BronzeSable.class, DauntlessOnslaught.class})
class AnaxAndCymedeTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic gives your creatures +1/+1 and trample")
    void heroicBoostsOwnCreaturesAndGrantsTrample() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, anax.getId());

        assertThat(anax.getPowerModifier()).isEqualTo(1);
        assertThat(anax.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Heroic bonuses wear off at end of turn")
    void heroicBonusesWearOffAtEndOfTurn() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, anax.getId());
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger heroic")
    void playerTargetDoesNotTriggerHeroic() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(anax.getPowerModifier()).isEqualTo(0);
        assertThat(anax.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell targeting Anax and Cymede does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, anax.getId());

        assertThat(anax.getPowerModifier()).isEqualTo(0);
        assertThat(anax.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A multi-target spell triggers heroic once and heroic resolves first")
    void multiTargetSpellTriggersHeroicOnceBeforeSpellResolves() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent sable = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(sable.getId(), anax.getId()));

        assertThat(anax.getPowerModifier()).isEqualTo(1);
        assertThat(anax.getToughnessModifier()).isEqualTo(1);
        assertThat(sable.getPowerModifier()).isEqualTo(1);
        assertThat(sable.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sable, Keyword.TRAMPLE)).isTrue();

        harness.passBothPriorities();

        assertThat(anax.getPowerModifier()).isEqualTo(3);
        assertThat(sable.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Heroic affects creatures present at resolution but not creatures entering later")
    void heroicSnapshotsCreaturesAtResolution() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, List.of(anax.getId()));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BronzeSable());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BronzeSable());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Successive spells targeting Anax and Cymede give cumulative heroic bonuses")
    void successiveSpellsGiveCumulativeBonuses() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent sable = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new DauntlessOnslaught(), new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, List.of(anax.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, List.of(anax.getId()));

        assertThat(sable.getPowerModifier()).isEqualTo(2);
        assertThat(sable.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sable, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A spell targeting only another creature does not trigger heroic")
    void otherCreatureTargetDoesNotTriggerHeroic() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent sable = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(sable.getId()));

        assertThat(anax.getPowerModifier()).isZero();
        assertThat(anax.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, anax, Keyword.TRAMPLE)).isFalse();
        assertThat(sable.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sable, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Heroic still boosts your creatures if Anax and Cymede dies in response")
    void heroicResolvesAfterSourceDies() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxAndCymede());
        Permanent sable = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(anax.getId()));
        harness.castAndResolveInstant(player2, 0, anax.getId());
        harness.assertInGraveyard(player1, "Anax and Cymede");
        harness.passBothPriorities();

        assertThat(sable.getPowerModifier()).isEqualTo(1);
        assertThat(sable.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sable, Keyword.TRAMPLE)).isTrue();
    }
}
