package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Uproot.class, GodsEyeGateToTheReikai.class, GnarledMass.class})
class UprootTest extends BaseCardTest {

    @Test
    void resolvingPutsTargetLandOnTopOfOwnersLibrary() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());
        Card oldTop = new GnarledMass();
        harness.setLibrary(player2, List.of(oldTop));

        prepareUproot();
        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land.getCard(), oldTop);
    }

    @Test
    void putsStolenLandOnItsOwnersLibrary() {
        Card landCard = new GodsEyeGateToTheReikai();
        landCard.setOwnerId(player2.getId());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, landCard);
        Card oldTop = new GnarledMass();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(oldTop));

        prepareUproot();
        harness.castAndResolveSorcery(player1, 0, stolenLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stolenLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(landCard, oldTop);
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        prepareUproot();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareUproot() {
        harness.setHand(player1, List.of(new Uproot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
