package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.b.BoulderbranchGolem;

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

@CardUsed({GnarlrootPallbearer.class, ArgothianSprite.class, BoulderbranchGolem.class, GiantGrowth.class})
class GnarlrootPallbearerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +X/+X for each creature card in its controller's graveyard")
    void etbBoostsTargetBasedOnControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new BoulderbranchGolem(), new GiantGrowth()));
        harness.setGraveyard(player2, List.of(new ArgothianSprite(), new BoulderbranchGolem()));
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Sprite");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Argothian Sprite");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void etbBoostWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Sprite");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Argothian Sprite");
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void countsGraveyardAtResolutionAndKeepsResolvedBoostFixed() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Argothian Sprite"));
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new BoulderbranchGolem(), new GiantGrowth()));
        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Argothian Sprite");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        harness.setGraveyard(player1, List.of());
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void emptyControllerGraveyardGivesZeroBoostDespiteOpponentCreatures() {
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        harness.setGraveyard(player2, List.of(new ArgothianSprite(), new BoulderbranchGolem()));
        harness.addToBattlefield(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Argothian Sprite"));
        resolveAllTriggers();

        Permanent target = findPermanent(player1, "Argothian Sprite");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterSourceLeavesAndCountsSourceInGraveyard() {
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Argothian Sprite"));
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Gnarlroot Pallbearer");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));
        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Argothian Sprite");
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetIsRemoved() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));
        harness.addToBattlefield(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Sprite");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
