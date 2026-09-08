package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TawnossCoffin.class, GrizzlyBears.class, HolyStrength.class, Twiddle.class, Shatter.class})
class TawnossCoffinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and its Auras, then returns both tapped with counters restored on untap")
    void returnsCreatureAndAurasOnUntap() {
        Permanent coffin = addReadyCoffin();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(creature.getId());

        activate(coffin, creature);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(creature.getCard().getId()));
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getId())
                .contains(creature.getCard().getId(), aura.getCard().getId());

        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, coffin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returnedCreature = findByCardId(player2, creature.getCard().getId());
        Permanent returnedAura = findByCardId(player2, aura.getCard().getId());
        assertThat(returnedCreature.isTapped()).isTrue();
        assertThat(returnedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Returns the exiled creature when the Coffin leaves the battlefield")
    void returnsCreatureWhenCoffinLeaves() {
        Permanent coffin = addReadyCoffin();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        activate(coffin, creature);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, coffin.getId());
        harness.passBothPriorities();

        Permanent returnedCreature = findByCardId(player2, creature.getCard().getId());
        assertThat(returnedCreature.isTapped()).isTrue();
        assertThat(returnedCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target only a creature")
    void onlyTargetsCreatures() {
        Permanent coffin = addReadyCoffin();
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(coffin);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCoffin() {
        Permanent coffin = harness.addToBattlefieldAndReturn(player1, new TawnossCoffin());
        coffin.setSummoningSick(false);
        return coffin;
    }

    private void activate(Permanent coffin, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(coffin);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent findByCardId(com.github.laxika.magicalvibes.model.Player player, java.util.UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
