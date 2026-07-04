package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TackleArtistTest extends BaseCardTest {

    private Permanent addArtist(Player player) {
        Permanent perm = new Permanent(new TackleArtist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Has spell-cast trigger with a 1-or-2 +1/+1 counter ConditionalReplacementEffect")
    void hasCorrectEffects() {
        TackleArtist card = new TackleArtist();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();

        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(ConditionalReplacementEffect.class);
        ConditionalReplacementEffect replacement =
                (ConditionalReplacementEffect) trigger.resolvedEffects().getFirst();
        assertThat(((SpellManaSpentAtLeast) replacement.condition()).minMana()).isEqualTo(5);

        PutCountersOnSelfEffect base = (PutCountersOnSelfEffect) replacement.baseEffect();
        assertThat(base.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        assertThat(base.count()).isEqualTo(1);

        PutCountersOnSelfEffect upgraded = (PutCountersOnSelfEffect) replacement.upgradedEffect();
        assertThat(upgraded.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        assertThat(upgraded.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a cheap instant adds one +1/+1 counter")
    void cheapSpellAddsOneCounter() {
        Permanent artist = addArtist(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(artist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a four-mana spell adds one +1/+1 counter (below threshold)")
    void fourManaSpellAddsOneCounter() {
        Permanent artist = addArtist(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(artist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a five-mana spell adds two +1/+1 counters instead")
    void fiveManaSpellAddsTwoCounters() {
        Permanent artist = addArtist(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(artist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent artist = addArtist(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(artist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
