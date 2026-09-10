package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GuiltyConscience.class)
class GuiltyConscienceTest extends BaseCardTest {

    @Test
    @DisplayName("Guilty Conscience deals the damage dealt by the enchanted creature back to it")
    void dealsDamageEqualToEnchantedCreaturesDamage() {
        Permanent creature = addCreatureReady(player2, createDamageCreature());

        harness.setHand(player1, List.of(new GuiltyConscience()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 3);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Guilty Conscience can enchant only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Card artifact = new Card();
        artifact.setName("Test Artifact");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{1}");
        Permanent artifactPermanent = harness.addToBattlefieldAndReturn(player2, artifact);

        harness.setHand(player1, List.of(new GuiltyConscience()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Card createDamageCreature() {
        Card card = new Card();
        card.setName("Test Pinger");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(3);
        card.setToughness(5);
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(3)),
                "{T}: Deal 3 damage to any target."
        ));
        return card;
    }
}
