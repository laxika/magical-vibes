package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AltarOfDementia;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DinaEssenceBrewer.class, AltarOfDementia.class, GrizzlyBears.class, Forest.class})
class DinaEssenceBrewerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gains life and puts counters equal to its power")
    void sacrificeAbilityUsesSacrificedPower() {
        Permanent dina = addReadyDina();
        addReadyCreature(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, dina.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(dina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The sacrifice trigger fires only once each turn")
    void sacrificeTriggerFiresOnlyOnceEachTurn() {
        addReadyDina();
        harness.addToBattlefield(player1, new AltarOfDementia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, player2.getId());
        chooseFirstSacrificeIfNeeded();
        resolveAll();
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAll();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Rejects a target creature controlled by an opponent")
    void rejectsOpponentCreatureTarget() {
        Permanent dina = addReadyDina();
        addReadyCreature(new GrizzlyBears());
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private Permanent addReadyDina() {
        return addReadyCreature(new DinaEssenceBrewer());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player1, card);
    }

    private void chooseFirstSacrificeIfNeeded() {
        if (gd.interaction.activeInteraction() != null) {
            UUID creatureId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                    .map(Permanent::getId)
                    .findFirst()
                    .orElseThrow();
            harness.handlePermanentChosen(player1, creatureId);
        }
    }

    private void resolveAll() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
