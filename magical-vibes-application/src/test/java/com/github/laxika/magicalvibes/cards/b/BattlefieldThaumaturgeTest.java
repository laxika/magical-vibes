package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.PinToTheEarth;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldThaumaturge.class, BountyOfMight.class, GiantGrowth.class, GrizzlyBears.class,
        IntoTheVoid.class, MindRot.class, PinToTheEarth.class, Shock.class})
class BattlefieldThaumaturgeTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces an instant or sorcery by one for each creature it targets")
    void reducesCostForEachCreatureTarget() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Does not reduce a spell that targets only a player")
    void doesNotReducePlayerTargetingSpell() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Heroic grants hexproof when you cast a spell targeting Battlefield Thaumaturge")
    void heroicGrantsHexproofWhenTargeted() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, thaumaturge.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Heroic hexproof wears off at end of turn")
    void heroicHexproofWearsOffAtEndOfTurn() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, thaumaturge.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Heroic does not trigger for a spell targeting a player")
    void heroicDoesNotTriggerForPlayerTarget() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Heroic does not trigger for an opponent's spell")
    void heroicDoesNotTriggerForOpponentsSpell() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID thaumaturgeId = thaumaturge.getId();
        harness.castAndResolveInstant(player2, 0, thaumaturgeId);

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void doesNotReduceAuraCost() {
        Permanent thaumaturge = harness.addToBattlefieldAndReturn(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new PinToTheEarth()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, thaumaturge.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void auraTriggersHeroicBeforeResolving() {
        Permanent thaumaturge = harness.addToBattlefieldAndReturn(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new PinToTheEarth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, thaumaturge.getId());
        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pin to the Earth");
    }

    @Test
    void twoCreatureTargetsReduceCostToOneGenericAndOneBlue() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void oneCreatureTargetDoesNotReceiveTwoReductions() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleThaumaturgesReduceOnlyGenericMana() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void doesNotReduceOpponentsSpell() {
        Permanent thaumaturge = harness.addToBattlefieldAndReturn(player1, new BattlefieldThaumaturge());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new IntoTheVoid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, List.of(thaumaturge.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReduceColoredManaRequirement() {
        Permanent thaumaturge = harness.addToBattlefieldAndReturn(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, thaumaturge.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetingSameCreatureThreeTimesReducesCostOnlyOnce() {
        Permanent thaumaturge = harness.addToBattlefieldAndReturn(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new BountyOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(thaumaturge.getId(), thaumaturge.getId(), thaumaturge.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
