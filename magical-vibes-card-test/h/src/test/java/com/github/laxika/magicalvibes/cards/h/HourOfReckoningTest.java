package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ScatterTheSeeds;
import com.github.laxika.magicalvibes.cards.v.VotaryOfTheConclave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HourOfReckoning.class, GrayscaledGharial.class, Forest.class, ScatterTheSeeds.class, VotaryOfTheConclave.class})
class HourOfReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nontoken creatures but leaves creature tokens")
    void destroysNontokenCreaturesButLeavesTokens() {
        harness.addToBattlefield(player1, new GrayscaledGharial());
        harness.addToBattlefield(player2, new GrayscaledGharial());
        Permanent token = addTokenCreature(player1);

        harness.castFromHand(player1, new HourOfReckoning(), "{4}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrayscaledGharial)
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player2, "Grayscaled Gharial");
    }

    @Test
    @DisplayName("Leaves noncreature permanents untouched")
    void leavesNoncreaturePermanentsUntouched() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrayscaledGharial());

        harness.castFromHand(player1, new HourOfReckoning(), "{4}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrayscaledGharial)
                .isEmpty();
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
    }

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        Permanent fourthCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new HourOfReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(
                firstCreature.getId(), secondCreature.getId(), thirdCreature.getId(), fourthCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();
        assertThat(fourthCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White creatures with summoning sickness can pay the colored convoke cost")
    void whiteCreaturesPayColoredCostWithConvoke() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave()),
                harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave()),
                harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave()),
                harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial()),
                harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial()),
                harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial()),
                harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial()));
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new HourOfReckoning()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hour of Reckoning");
        harness.assertInGraveyard(player1, "Votary of the Conclave");
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
    }

    @Test
    @DisplayName("Convoke tokens survive the destruction, as do opposing creature tokens")
    void convokeTokensAndOpposingTokensSurvive() {
        harness.castFromHand(player1, new ScatterTheSeeds(), "{3}{G}{G}");
        harness.passBothPriorities();
        List<Permanent> ownTokens = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        harness.castFromHand(player2, new ScatterTheSeeds(), "{3}{G}{G}");
        harness.passBothPriorities();
        List<Permanent> opposingTokens = List.copyOf(gd.playerBattlefields.get(player2.getId()));
        harness.addToBattlefield(player1, new GrayscaledGharial());
        harness.addToBattlefield(player2, new GrayscaledGharial());
        harness.setHand(player1, List.of(new HourOfReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                ownTokens.stream().map(Permanent::getId).toList());
        assertThat(ownTokens).hasSize(3).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyElementsOf(ownTokens);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyElementsOf(opposingTokens);
        assertThat(opposingTokens).hasSize(3);
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player2, "Grayscaled Gharial");
    }

    @Test
    @DisplayName("A nontoken creature can regenerate from Hour of Reckoning")
    void allowsRegeneration() {
        Permanent votary = harness.addToBattlefieldAndReturn(player1, new VotaryOfTheConclave());
        harness.addToBattlefield(player1, new GrayscaledGharial());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new HourOfReckoning(), "{4}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(votary);
        assertThat(votary.isTapped()).isTrue();
        assertThat(votary.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.assertNotInGraveyard(player1, "Votary of the Conclave");
    }

    private Permanent addTokenCreature(com.github.laxika.magicalvibes.model.Player player) {
        GrayscaledGharial tokenCard = new GrayscaledGharial();
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }
}
