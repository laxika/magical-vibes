package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.p.PestilenceRats;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
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

@CardUsed({SplashPortal.class, ZephyrFalcon.class, Frogmite.class, KitsaOtterballElite.class,
        PestilenceRats.class, GrizzlyBears.class})
class SplashPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers each listed creature subtype and draws a card")
    void flickersListedCreatureSubtypesAndDraws() {
        assertFlickersAndDraws(new ZephyrFalcon());
        assertFlickersAndDraws(new Frogmite());
        assertFlickersAndDraws(new KitsaOtterballElite());
        assertFlickersAndDraws(new PestilenceRats());
    }

    @Test
    @DisplayName("Flickers an unlisted creature without drawing a card")
    void flickersUnlistedCreatureWithoutDrawing() {
        assertFlickersWithoutDrawing(new GrizzlyBears());
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertFlickersAndDraws(Card creature) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void assertFlickersWithoutDrawing(Card creature) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new SplashPortal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }
}
