package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResurrectedCultist.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class})
class ResurrectedCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter when delirium is active")
    void returnsFromGraveyardWithFinalityCounter() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(cultist);
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot activate without delirium")
    void cannotActivateWithoutDelirium() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, List.of(cultist, new GrizzlyBears(), new Forest(), new Shock()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Resurrected Cultist");
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Finality counter exiles the returned Cultist instead of putting it into a graveyard")
    void finalityCounterExilesWhenItDies() {
        prepareMainPhase();
        ResurrectedCultist cultist = new ResurrectedCultist();
        harness.setGraveyard(player1, deliriumGraveyard(cultist));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(cultist);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Resurrected Cultist");
        harness.assertNotInGraveyard(player1, "Resurrected Cultist");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Resurrected Cultist"));
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deliriumGraveyard(Card source) {
        return new ArrayList<>(List.of(source, new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }
}
