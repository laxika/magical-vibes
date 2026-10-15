package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.c.CaptainsCall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Ixidron;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.r.ResoluteReinforcements;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueenAllenalOfRuadach.class, BladeSplicer.class, GrizzlyBears.class, WilyGoblin.class,
        CaptainsCall.class, ResoluteReinforcements.class, Ixidron.class, ParallelLives.class})
class QueenAllenalOfRuadachTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessEqualControlledCreatures() {
        Permanent queen = addQueenReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one Soldier to a creature-token creation event")
    void addsSoldierToCreatureTokenCreation() {
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Does not add a Soldier to a noncreature-token creation event")
    void doesNotAddSoldierToNoncreatureTokenCreation() {
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private Permanent addQueenReady(Player player) {
        return addCreatureReady(player, new QueenAllenalOfRuadach());
    }

    @Test
    @CardUsed({CaptainsCall.class})
    void addsOnlyOneSoldierToAnEntireBatchAndCountsAllNewCreatures() {
        Permanent queen = addQueenReady(player1);
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(5);
    }

    @Test
    @CardUsed({ResoluteReinforcements.class})
    void doesNotAddSoldiersToOpponentCreatureTokens() {
        Permanent queen = addQueenReady(player1);
        harness.setHand(player2, List.of(new ResoluteReinforcements()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Soldier")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(1);
    }

    @Test
    @CardUsed({Ixidron.class, CaptainsCall.class})
    void faceDownQueenDoesNotAddSoldiers() {
        Permanent queen = addQueenReady(player1);
        harness.setHand(player1, List.of(new Ixidron(), new CaptainsCall()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(queen.isFaceDown()).isTrue();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
    }

    @Test
    @CardUsed({CaptainsCall.class, ParallelLives.class})
    void doesNotSilentlyForceTokenDoublingBeforeQueenReplacement() {
        addQueenReady(player1);
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.isAwaitingInput() || countPermanents(player1, "Soldier") == 8)
                .as("Offer replacement ordering or apply Queen before Parallel Lives to create eight Soldiers")
                .isTrue();
    }
}
