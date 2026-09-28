package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzebeakForagers.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class BronzebeakForagersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one nonland permanent per opponent until it leaves")
    void etbExilesOneNonlandPermanentPerOpponentUntilItLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent foragers = castForagers(List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getCardsExiledByPermanent(foragers.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getCard().getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, foragers.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target two nonland permanents controlled by the same opponent")
    void etbTargetsAtMostOnePermanentPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForagersCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability puts an exiled card into its owner's graveyard and gains X life")
    void activatedAbilityPutsExiledCardIntoGraveyardAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent foragers = castForagers(List.of(bears.getId()));
        UUID exiledCardId = gd.getCardsExiledByPermanent(foragers.getId()).getFirst().getId();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 2, exiledCardId, Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCardId)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    private Permanent castForagers(List<UUID> targetIds) {
        prepareForagersCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Bronzebeak Foragers");
    }

    private void prepareForagersCast() {
        harness.setHand(player1, List.of(new BronzebeakForagers()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
