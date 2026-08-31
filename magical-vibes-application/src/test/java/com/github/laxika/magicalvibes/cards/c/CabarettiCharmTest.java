package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabarettiCharm.class, ChandraNalaar.class, FountainOfYouth.class, GrizzlyBears.class,
        HillGiant.class})
class CabarettiCharmTest extends BaseCardTest {

    private void addRGW() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Mode 0 deals damage equal to the number of creatures you control")
    void modeZeroDealsDamageEqualToCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CabarettiCharm()));
        addRGW();

        harness.castModalInstant(player1, 0, 0, List.of(hillGiant.getId()));
        harness.passBothPriorities();

        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 0 can target a planeswalker")
    void modeZeroDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new CabarettiCharm()));
        addRGW();

        harness.castModalInstant(player1, 0, 0, List.of(chandra.getId()));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mode 0 cannot target a noncreature nonplaneswalker permanent")
    void modeZeroRejectsInvalidTarget() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CabarettiCharm()));
        addRGW();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(fountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Mode 1 boosts your creatures and gives them trample until end of turn")
    void modeOneBoostsOwnCreaturesAndGrantsTrample() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CabarettiCharm()));
        addRGW();

        harness.castModalInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(ownBear.getEffectivePower()).isEqualTo(3);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opposingBear.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBear.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Mode 2 creates two green and white Citizen tokens")
    void modeTwoCreatesTwoCitizenTokens() {
        harness.setHand(player1, List.of(new CabarettiCharm()));
        addRGW();

        harness.castModalInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(2);
        assertThat(citizens).allSatisfy(citizen -> {
            assertThat(citizen.getEffectivePower()).isEqualTo(1);
            assertThat(citizen.getEffectiveToughness()).isEqualTo(1);
            assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(citizen.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        });
    }
}
