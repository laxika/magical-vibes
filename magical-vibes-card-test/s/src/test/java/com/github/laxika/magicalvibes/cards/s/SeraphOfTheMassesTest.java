package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphOfTheMasses.class, GrizzlyBears.class})
class SeraphOfTheMassesTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessEqualControlledCreatures() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheMasses());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can be cast using convoke")
    void castsWithConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeraphOfTheMasses()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent seraph = findPermanent(player1, "Seraph of the Masses");
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness update when creatures enter and leave")
    void updatesWhenCreatureCountChanges() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player1, new SeraphOfTheMasses());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(1);

        Permanent other = harness.enterBattlefieldAndReturn(player1, new SeraphOfTheMasses());

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(2);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other);

        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(1);
    }

    @Test
    @DisplayName("Characteristic power and toughness work in hand and graveyard without counting the card itself")
    void creatureCountAppliesOutsideBattlefield() {
        SeraphOfTheMasses inHand = new SeraphOfTheMasses();
        SeraphOfTheMasses inGraveyard = new SeraphOfTheMasses();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new SeraphOfTheMasses());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new SeraphOfTheMasses());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying prevents ground creatures from blocking but allows flying blockers")
    void flyingRestrictsBlockers() {
        Permanent attacker = addCreatureReady(player1, new SeraphOfTheMasses());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new SeraphOfTheMasses());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick white creatures can convoke the white mana requirements")
    void whiteCreaturesConvokeColoredManaWhileSummoningSick() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SeraphOfTheMasses());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SeraphOfTheMasses());
        harness.setHand(player1, List.of(new SeraphOfTheMasses()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Seraph of the Masses")).isEqualTo(3);
        for (Permanent seraph : findPermanents(player1, "Seraph of the Masses")) {
            assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
        }
    }
}
