package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.s.SilentDeparture;
import com.github.laxika.magicalvibes.cards.v.VanquishTheHorde;
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

@CardUsed({LunarchVeteran.class, CatharCommando.class, VanquishTheHorde.class, SilentDeparture.class})
class LunarchVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LunarchVeteran());
        harness.castFromHand(player1, new CatharCommando(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when it enters itself")
    void noLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new LunarchVeteran(), "{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Luminous Phantom")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent phantom = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(phantom.isTransformed()).isTrue();
        assertThat(phantom.getCard().getName()).isEqualTo("Luminous Phantom");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Luminous Phantom gains life when another creature you control leaves")
    void phantomGainsLifeOnAllyCreatureLeaves() {
        harness.setLife(player1, 20);
        Permanent phantom = putTransformedPhantomOnBattlefield();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CatharCommando());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ally));
        harness.passBothPriorities(); // resolve leave trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(phantom);
    }

    @Test
    @DisplayName("Luminous Phantom is exiled instead of going to the graveyard")
    void phantomExiledInsteadOfGraveyard() {
        Permanent phantom = putTransformedPhantomOnBattlefield();
        UUID phantomId = phantom.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, phantom));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(phantomId);
    }

    @Test
    void opponentCreatureEnteringDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LunarchVeteran());
        harness.enterBattlefieldAndReturn(player2, new CatharCommando());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void phantomDoesNotGainLifeWhenAnotherCreatureEnters() {
        putTransformedPhantomOnBattlefield();
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new CatharCommando());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void phantomDoesNotGainLifeWhenItLeaves() {
        Permanent phantom = putTransformedPhantomOnBattlefield();
        harness.setLife(player1, 20);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, phantom));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void phantomDoesNotGainLifeWhenOpponentCreatureLeaves() {
        putTransformedPhantomOnBattlefield();
        harness.setLife(player1, 20);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CatharCommando());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponentCreature));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void phantomGainsLifeWhenAllyIsReturnedToHand() {
        putTransformedPhantomOnBattlefield();
        harness.setLife(player1, 20);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CatharCommando());
        harness.setHand(player1, List.of(new SilentDeparture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, ally.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Cathar Commando");
    }

    @Test
    void bouncedPhantomReturnsAsVeteranAndCanGoToGraveyard() {
        Permanent phantom = putTransformedPhantomOnBattlefield();
        UUID physicalId = phantom.getOriginalCard().getId();
        harness.setHand(player1, List.of(new SilentDeparture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, phantom.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Lunarch Veteran");
        assertThat(gd.exiledCards).isEmpty();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent veteran = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(veteran.isTransformed()).isFalse();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, veteran));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(card -> card.getId())).contains(physicalId);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void phantomGainsLifeForAllyLeavingSimultaneouslyWithIt() {
        Permanent phantom = putTransformedPhantomOnBattlefield();
        UUID physicalId = phantom.getOriginalCard().getId();
        harness.addToBattlefield(player1, new CatharCommando());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new VanquishTheHorde(), "{6}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(physicalId);
        harness.assertInGraveyard(player1, "Cathar Commando");
    }

    private Permanent putTransformedPhantomOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
