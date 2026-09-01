package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrsCry.class, BogRats.class, FountainOfYouth.class, Squire.class})
class MartyrsCryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all white creatures and each affected controller draws for their exiled creatures")
    void exilesWhiteCreaturesAndDrawsForEachController() {
        var player1WhiteCreature = new Squire();
        var player1SecondWhiteCreature = new Squire();
        var player2WhiteCreature = new Squire();
        harness.addToBattlefield(player1, player1WhiteCreature);
        harness.addToBattlefield(player1, player1SecondWhiteCreature);
        harness.addToBattlefield(player1, new BogRats());
        harness.addToBattlefield(player2, player2WhiteCreature);
        harness.addToBattlefield(player2, new BogRats());
        var player1FirstDraw = new FountainOfYouth();
        var player1SecondDraw = new FountainOfYouth();
        var player2Draw = new FountainOfYouth();
        harness.setLibrary(player1, List.of(player1FirstDraw, player1SecondDraw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new MartyrsCry(), "{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Squire");
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertOnBattlefield(player1, "Bog Rats");
        harness.assertOnBattlefield(player2, "Bog Rats");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(player1WhiteCreature, player1SecondWhiteCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(player2WhiteCreature);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(player1FirstDraw, player1SecondDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
    }

    @Test
    @DisplayName("Affects white creatures on the battlefield, not white creatures in other zones")
    void affectsOnlyWhiteCreaturesOnBattlefield() {
        var battlefieldCreature = new Squire();
        var handCreature = new Squire();
        var graveyardCreature = new Squire();
        var draw = new FountainOfYouth();

        harness.addToBattlefield(player1, battlefieldCreature);
        harness.setHand(player1, List.of(new MartyrsCry(), handCreature));
        gd.playerGraveyards.get(player1.getId()).add(graveyardCreature);
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(battlefieldCreature);
        assertThat(gd.playerHands.get(player1.getId())).contains(handCreature, draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
    }

    @Test
    @DisplayName("The current controller draws while the exiled creature goes to its owner's exile")
    void drawsForCurrentControllerOfStolenCreature() {
        var stolenCreature = new Squire();
        stolenCreature.setOwnerId(player2.getId());
        var stolenPermanent = harness.addToBattlefieldAndReturn(player2, stolenCreature);
        gd.stolenCreatures.put(stolenPermanent.getId(), player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), stolenPermanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        var player1Draw = new FountainOfYouth();
        var player2Draw = new FountainOfYouth();

        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.castFromHand(player1, new MartyrsCry(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Draw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stolenCreature);
    }
}
