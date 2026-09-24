package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraParagon.class, Forest.class, WalkingCorpse.class, Shock.class, StoneRain.class})
class SerraParagonTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent cast from the graveyard gains the exile and life trigger")
    void permanentCastFromGraveyardGainsTrigger() {
        harness.addToBattlefield(player1, new SerraParagon());
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent corpsePermanent = findPermanent(player1, "Walking Corpse");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, corpsePermanent.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Walking Corpse");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Walking Corpse");
    }

    @Test
    @DisplayName("Playing a land from the graveyard uses Serra Paragon's once-per-turn permission")
    void playingLandUsesOncePerTurnPermission() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest(), new WalkingCorpse()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land played from the graveyard gains the exile and life trigger")
    void landPlayedFromGraveyardGainsTrigger() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, forest.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Casting a permanent from the graveyard uses the same permission as playing a land")
    void castingPermanentUsesOncePerTurnPermission() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
