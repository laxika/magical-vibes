package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormForceOfNature.class, GrizzlyBears.class})
class StormForceOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives the next instant Storm for each prior spell")
    void combatDamageGivesNextInstantStorm() {
        addStormReady();
        gd.recordSpellCast(player1.getId(), lifeGainInstant());
        gd.recordSpellCast(player2.getId(), lifeGainInstant());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player1, List.of(lifeGainInstant(), lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("An intervening creature spell does not consume the Storm grant")
    void creatureSpellDoesNotConsumeStormGrant() {
        addStormReady();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    private Permanent addStormReady() {
        return addCreatureReady(player1, new StormForceOfNature());
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
