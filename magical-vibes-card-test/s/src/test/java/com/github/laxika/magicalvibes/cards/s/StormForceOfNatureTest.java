package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormForceOfNature.class, DarkRitual.class, GrizzlyBears.class, SacredNectar.class})
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

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
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

    @Test
    @DisplayName("The first spell consumes the grant even when storm makes no copies")
    void zeroPriorSpellsStillConsumesGrant() {
        addStormReady();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);

        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's instant contributes to storm without consuming the grant")
    void opponentsInstantDoesNotConsumeGrant() {
        addStormReady();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(3);

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(6);
    }

    @Test
    @DisplayName("The next sorcery gains storm and counts spells cast before combat")
    void sorceryGetsStorm() {
        addStormReady();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new SacredNectar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("An unused storm grant expires when the turn ends")
    void grantExpiresAtEndOfTurn() {
        addStormReady();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
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
