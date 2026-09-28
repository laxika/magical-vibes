package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarmenCruelSkymarcher.class, GrizzlyBears.class, HillGiant.class,
        KuldothaRebirth.class, Spellbook.class})
class CarmenCruelSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("A sacrificed permanent puts a counter on Carmen and gains 1 life")
    void sacrificedPermanentGrowsCarmenAndGainsLife() {
        Permanent carmen = addReadyCarmen(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLife(player1, 10);

        castKuldothaRebirth(player1, artifact);
        harness.passBothPriorities();

        assertThat(carmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Attacking returns an eligible permanent card from the graveyard")
    void attackReturnsPermanentWithinCarmensPower() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        addReadyCarmen(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void castKuldothaRebirth(com.github.laxika.magicalvibes.model.Player player,
                                     Permanent artifact) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new KuldothaRebirth()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, artifact.getId());
    }

    private Permanent addReadyCarmen(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new CarmenCruelSkymarcher());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
