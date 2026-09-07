package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({ShadesBreath.class, GrizzlyBears.class})
class ShadesBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature you control becomes a black Shade until end of turn")
    void transformsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShadesBreath();

        assertThat(gqs.hasColor(gd, ownCreature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, ownCreature, CardColor.GREEN)).isFalse();
        assertThat(ownCreature.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.SHADE);
        assertThat(gqs.hasColor(gd, opponentCreature, CardColor.GREEN)).isTrue();
        assertThat(opponentCreature.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Each transformed creature can activate its granted pump ability")
    void grantsPumpAbilityToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castShadesBreath();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The transformation and granted ability wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castShadesBreath();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasColor(gd, ownCreature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, ownCreature, CardColor.BLACK)).isFalse();
        assertThat(ownCreature.getTransientCreatureTypeOverride()).isNull();
        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.activateAbility(player1, 0, 0, null, null);
        }).isInstanceOf(IllegalStateException.class);
    }

    private void castShadesBreath() {
        harness.setHand(player1, List.of(new ShadesBreath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
