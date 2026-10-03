package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AltarOfDementia;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighMarket;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
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

@CardUsed({DinaEssenceBrewer.class, AltarOfDementia.class, GrizzlyBears.class, Forest.class,
        HighMarket.class, SakuraTribeElder.class})
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
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null, player2.getId());
        chooseFirstSacrificeIfNeeded();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Rejects a target creature controlled by an opponent")
    void rejectsOpponentCreatureTarget() {
        addReadyDina();
        addReadyCreature(new GrizzlyBears());
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Sacrificing Dina after her trigger has fired does not draw again")
    void sacrificingDinaDoesNotResetOncePerTurnLimit() {
        Permanent dina = addReadyDina();
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player1, new HighMarket());
        harness.setLibrary(player1, List.of(new HighMarket(), new HighMarket(), new HighMarket()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, dina.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dina, Essence Brewer");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing Dina herself draws when she has not triggered this turn")
    void sacrificingDinaTriggersHerOwnAbility() {
        addReadyDina();
        harness.addToBattlefield(player1, new HighMarket());
        harness.setLibrary(player1, List.of(new HighMarket(), new HighMarket()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dina, Essence Brewer");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The sacrificed creature's counters contribute to its last known power")
    void sacrificeUsesModifiedPower() {
        Permanent dina = addReadyDina();
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        elder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new HighMarket(), new HighMarket()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, dina.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(dina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("Sacrificing the targeted creature prevents both life gain and counters")
    void sacrificedTargetMakesEntireAbilityFailToResolve() {
        Permanent dina = addReadyDina();
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.setLibrary(player1, List.of(new HighMarket(), new HighMarket()));
        harness.setLife(player1, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, elder.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(dina.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("An opponent's creature sacrifice does not trigger Dina")
    void opponentSacrificeDoesNotDraw() {
        addReadyDina();
        harness.addToBattlefield(player2, new HighMarket());
        harness.addToBattlefield(player2, new SakuraTribeElder());
        harness.setLibrary(player1, List.of(new HighMarket(), new HighMarket()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Sakura-Tribe Elder");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
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

}
