package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PassTheTorch.class, GrizzlyBears.class, Shock.class})
class PassTheTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage and lets you choose a creature card in hand for the perpetual ability")
    void dealsDamageAndChoosesCreatureCard() {
        harness.setHand(player1, List.of(new PassTheTorch(), new GrizzlyBears(), new Shock()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        PendingInteraction.PerpetualTriggeredAbilityCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualTriggeredAbilityCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        var selected = gd.playerHands.get(player1.getId()).get(0);
        assertThat(gd.perpetualTriggeredAbilityGrants.get(selected.getId()))
                .containsKey(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
        assertThat(gd.perpetualTriggeredAbilityGrants.get(selected.getId())
                .get(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER))
                .anyMatch(CastTargetInstantOrSorceryFromGraveyardEffect.class::isInstance);
    }
}
