package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BringToLife;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimatingFaerie.class, BringToLife.class, GoldenEgg.class, HardenedScales.class,
        IronMyr.class, MindStone.class})
class AnimatingFaerieTest extends BaseCardTest {

    @Test
    void animationHappensBeforeCreatureCounterReplacement() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, egg.getId());
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(5);
    }

    @Test
    void adventureExilesCardAndAllowsCastingCreatureFace() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        AnimatingFaerie card = new AnimatingFaerie();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, egg.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Animating Faerie");

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Animating Faerie");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gqs.isCreature(gd, egg)).isTrue();
    }

    @Test
    void creatureFaceCanBeCastWithoutTakingAdventure() {
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Animating Faerie");
    }

    @Test
    void adventureCannotTargetNonartifactPermanent() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new AnimatingFaerie());
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animationAddsFourCountersWithoutRemovingExistingCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        egg.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, egg.getId());
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(6);
    }

    @Test
    void animationAndCountersPersistAfterTurnEnds() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, egg.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(4);
    }

    @Test
    void adventureWithRemovedTargetGoesToGraveyardInsteadOfExile() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        AnimatingFaerie card = new AnimatingFaerie();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAdventure(player1, 0, egg.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Golden Egg");
        harness.assertInGraveyard(player1, "Animating Faerie");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void bringToLifeAnimatesControlledNoncreatureArtifactWithFourCounters() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        AnimatingFaerie card = new AnimatingFaerie();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, mindStone.getId());
        harness.passBothPriorities();

        assertThat(mindStone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, mindStone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mindStone)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mindStone)).isEqualTo(4);
        assertThat(mindStone.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void bringToLifeCannotTargetArtifactCreatureOrOpponentsArtifact() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        AnimatingFaerie firstCard = new AnimatingFaerie();
        harness.setHand(player1, List.of(firstCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        AnimatingFaerie secondCard = new AnimatingFaerie();
        harness.setHand(player1, List.of(secondCard));

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
