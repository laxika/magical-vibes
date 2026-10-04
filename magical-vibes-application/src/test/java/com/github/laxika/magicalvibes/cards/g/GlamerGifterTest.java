package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlamerGifter.class, FountainOfYouth.class, GoblinKing.class, GrizzlyBears.class, Unsummon.class})
class GlamerGifterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sets another target creature to base 4/4 and grants all creature types")
    void etbSetsBaseStatsAndGrantsAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("ETB effects wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("ETB can resolve without a target when no other creature exists")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("Can choose no target even when another creature is available")
    void canDeclineAnAvailableTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature without removing its existing abilities")
    void canTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlamerGifter());
        harness.setHand(player1, List.of(new GlamerGifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.CHANGELING)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("ETB cannot target Glamer Gifter itself")
    void cannotTargetItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new GlamerGifter());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, source, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("ETB still resolves after Glamer Gifter leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Glamer Gifter");
        harness.castInstant(player1, 0, sourceId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GlamerGifter);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("An ETB with a removed target does not affect the creature when it returns")
    void removedTargetIsNotAffectedOnReentry() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlamerGifter(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, returnedBears, CardSubtype.GOBLIN)).isFalse();
    }
}
