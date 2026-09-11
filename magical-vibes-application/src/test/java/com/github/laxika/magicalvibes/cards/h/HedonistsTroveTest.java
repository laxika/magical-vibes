package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HedonistsTrove.class, Forest.class, GrizzlyBears.class, Shock.class})
class HedonistsTroveTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles and tracks the targeted opponent's graveyard")
    void exilesTargetOpponentsGraveyardWithTrove() {
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        Permanent trove = castTrove(List.of(land, creature));

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(trove.getId()))
                .containsExactly(land, creature);
    }

    @Test
    @DisplayName("Controller may play one tracked land and cast one tracked spell each turn")
    void playsLandAndCastsOneSpellFromTrove() {
        Forest land = new Forest();
        GrizzlyBears firstSpell = new GrizzlyBears();
        Shock secondSpell = new Shock();
        Permanent trove = castTrove(List.of(land, firstSpell, secondSpell));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        harness.assertOnBattlefield(player1, "Forest");

        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, firstSpell.getId(), null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, secondSpell.getId(), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(secondSpell);
    }

    @Test
    @DisplayName("A tracked land can still be played after the turn's tracked spell")
    void playsLandAfterCastingSpellFromTrove() {
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        castTrove(List.of(land, spell));

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Trove cannot target its controller")
    void cannotTargetController() {
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new HedonistsTrove()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Permanent castTrove(List<Card> graveyard) {
        harness.setGraveyard(player2, graveyard);
        harness.setHand(player1, List.of(new HedonistsTrove()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID troveId = harness.getPermanentId(player1, "Hedonist's Trove");
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(troveId))
                .findFirst()
                .orElseThrow();
    }
}
