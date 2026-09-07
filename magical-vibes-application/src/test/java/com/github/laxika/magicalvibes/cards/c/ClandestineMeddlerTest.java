package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClandestineMeddler.class, GrizzlyBears.class})
class ClandestineMeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB suspects up to one other creature you control")
    void entersAndSuspectsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castMeddler(bear.getId());

        assertThat(bear.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("ETB may choose no creature")
    void entersWithoutSuspectingWhenNoTargetIsChosen() {
        castMeddler(null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.isSuspected());
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClandestineMeddler()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surveils once when one or more suspected creatures attack")
    void surveilsOnceForSuspectedAttackers() {
        Permanent meddler = addCreatureReady(player1, new ClandestineMeddler());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        firstBear.setSuspected(true);
        secondBear.setSuspected(true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(meddler.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("Does not surveil when no suspected creature attacks")
    void doesNotSurveilForUnsuspectedAttackers() {
        addCreatureReady(player1, new ClandestineMeddler());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(bear.isSuspected()).isFalse();
    }

    private void castMeddler(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ClandestineMeddler()));
        addMana();
        harness.castCreature(player1, 0, targetId == null ? List.of() : List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
