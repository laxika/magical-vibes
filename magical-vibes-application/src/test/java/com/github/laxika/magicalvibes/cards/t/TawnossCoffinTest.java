package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TawnossCoffin.class, GrizzlyBears.class, Twiddle.class, HolyStrength.class, Disenchant.class})
class TawnossCoffinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with its counters and returns it when the Coffin untaps")
    void returnsCreatureWhenCoffinUntaps() {
        Permanent coffin = harness.addToBattlefieldAndReturn(player1, new TawnossCoffin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 1);
        UUID creatureCardId = creature.getCard().getId();

        activateCoffin(coffin, creature.getId());

        assertThat(gd.getCardsExiledByPermanent(coffin.getId()))
                .extracting(card -> card.getId())
                .containsExactly(creatureCardId);
        assertThat(findPermanent(player1, creatureCardId)).isNull();

        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, coffin.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returnedCreature = findPermanent(player1, creatureCardId);
        assertThat(returnedCreature).isNotNull();
        assertThat(returnedCreature.isTapped()).isTrue();
        assertThat(returnedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returnedCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns attached Auras attached to the returned creature when the Coffin leaves")
    void returnsAttachedAurasWhenCoffinLeaves() {
        Permanent coffin = harness.addToBattlefieldAndReturn(player1, new TawnossCoffin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID creatureCardId = creature.getCard().getId();
        UUID auraCardId = aura.getCard().getId();

        activateCoffin(coffin, creature.getId());

        assertThat(gd.getCardsExiledByPermanent(coffin.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(creatureCardId, auraCardId);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, coffin.getId());

        Permanent returnedCreature = findPermanent(player1, creatureCardId);
        Permanent returnedAura = findPermanent(player1, auraCardId);
        assertThat(returnedCreature).isNotNull();
        assertThat(returnedCreature.isTapped()).isTrue();
        assertThat(returnedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedAura).isNotNull();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent coffin = harness.addToBattlefieldAndReturn(player1, new TawnossCoffin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(coffin), null, coffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void activateCoffin(Permanent coffin, UUID targetId) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(coffin), null, targetId);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent findPermanent(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
