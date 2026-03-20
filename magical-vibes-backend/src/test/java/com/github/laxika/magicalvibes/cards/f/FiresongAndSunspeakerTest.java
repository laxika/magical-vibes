package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WhitesunsPassage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnSpellLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FiresongAndSunspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Firesong and Sunspeaker has correct effects registered")
    void hasCorrectEffects() {
        FiresongAndSunspeaker card = new FiresongAndSunspeaker();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantLifelinkToControllerSpellsByColorEffect.class);
        GrantLifelinkToControllerSpellsByColorEffect staticEffect =
                (GrantLifelinkToControllerSpellsByColorEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(staticEffect.color()).isEqualTo(CardColor.RED);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE).getFirst())
                .isInstanceOf(DealDamageOnSpellLifeGainEffect.class);
        DealDamageOnSpellLifeGainEffect triggerEffect =
                (DealDamageOnSpellLifeGainEffect) card.getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE).getFirst();
        assertThat(triggerEffect.damage()).isEqualTo(3);
        assertThat(triggerEffect.triggeringColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Red instant targeting player: deals damage + controller gains life from spell lifelink")
    void redInstantTargetingPlayerGrantsLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Shock deals 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Spell lifelink: player1 gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Red instant targeting creature: deals damage + controller gains life from spell lifelink")
    void redInstantTargetingCreatureGrantsLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // Shock deals 2 damage to Grizzly Bears (kills it)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Spell lifelink: player1 gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("White sorcery life gain triggers ability 2: deals 3 damage to chosen target")
    void whiteSorceryLifeGainTriggersDealDamage() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Whitesun's Passage gains 5 life and triggers ability 2
        // Now we need to choose a target for the triggered ability
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);

        // Choose player2 as the target for the 3 damage
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // Firesong and Sunspeaker deals 3 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Non-red spell does not get lifelink")
    void nonRedSpellDoesNotGetLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Whitesun's Passage gains 5 life (it's white, not red, so no lifelink bonus)
        // The only life gain is the 5 from the spell itself
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Non-white spell life gain (from lifelink) does not trigger ability 2")
    void nonWhiteSpellLifelinkDoesNotTriggerAbility2() {
        // Shock is red, not white. Its lifelink life gain should NOT trigger ability 2.
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Shock deals 2 damage, lifelink grants 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        // No pending trigger targets — ability 2 should not have triggered
        assertThat(gd.pendingLifeGainTriggerTargets).isEmpty();
        // Stack should be empty (no triggered ability was queued)
        assertThat(gd.stack).isEmpty();
    }
}
