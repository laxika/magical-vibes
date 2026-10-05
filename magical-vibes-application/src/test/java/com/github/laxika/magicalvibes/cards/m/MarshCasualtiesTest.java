package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshCasualties.class, HillGiant.class, KrakenHatchling.class, StoneworkPuma.class})
class MarshCasualtiesTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, gives the target player's creatures -1/-1")
    void withoutKickerWeakensTargetPlayersCreatures() {
        Permanent own = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarshCasualties()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("With kicker, gives the target player's creatures -2/-2")
    void withKickerWeakensTargetPlayersCreaturesMore() {
        Permanent own = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarshCasualties()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The creature debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarshCasualties()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target its controller and weakens every creature they control")
    void canTargetSelfAndAffectMultipleCreatures() {
        Permanent first = addCreatureReady(player1, new StoneworkPuma());
        Permanent second = addCreatureReady(player1, new KrakenHatchling());
        Permanent opponent = addCreatureReady(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new MarshCasualties()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(first.getEffectivePower()).isEqualTo(1);
        assertThat(first.getEffectiveToughness()).isEqualTo(1);
        assertThat(second.getEffectivePower()).isEqualTo(-1);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kicked debuff kills two-toughness creatures and expires for survivors")
    void kickedDebuffKillsSmallCreaturesAndExpires() {
        addCreatureReady(player2, new StoneworkPuma());
        Permanent survivor = addCreatureReady(player2, new KrakenHatchling());
        harness.setHand(player1, List.of(new MarshCasualties()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Stonework Puma");
        harness.assertInGraveyard(player2, "Stonework Puma");
        assertThat(survivor.getEffectivePower()).isEqualTo(-2);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(survivor.getEffectivePower()).isZero();
        assertThat(survivor.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void laterCreaturesAreNotAffected() {
        Permanent affected = addCreatureReady(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new MarshCasualties(), new StoneworkPuma()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(affected.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(affected.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getPowerModifier()).isZero();
                    assertThat(permanent.getToughnessModifier()).isZero();
                });
    }
}
