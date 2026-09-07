package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Petradon.class, DoomBlade.class, Forest.class, Mountain.class})
class PetradonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles two target lands and tracks them with Petradon")
    void etbExilesTwoTargetLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        Permanent petradon = castAndResolvePetradon(forest, mountain);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(gd.getCardsExiledByPermanent(petradon.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
    }

    @Test
    @DisplayName("The exiled lands return under their owners' control when Petradon leaves")
    void exiledLandsReturnWhenPetradonLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent petradon = castAndResolvePetradon(forest, mountain);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, petradon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petradon");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.getCardsExiledByPermanent(petradon.getId())).isEmpty();
    }

    @Test
    @DisplayName("Red mana gives Petradon +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent petradon = addReadyPetradon();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(petradon.getPowerModifier()).isEqualTo(1);
        assertThat(petradon.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(petradon.getPowerModifier()).isZero();
        assertThat(petradon.getToughnessModifier()).isZero();
    }

    private Permanent castAndResolvePetradon(Permanent firstLand, Permanent secondLand) {
        harness.setHand(player1, List.of(new Petradon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        return findPermanent(player1, "Petradon");
    }

    private Permanent addReadyPetradon() {
        Permanent petradon = new Permanent(new Petradon());
        petradon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(petradon);
        return petradon;
    }
}
