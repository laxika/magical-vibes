package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.k.KazanduMammoth;
import com.github.laxika.magicalvibes.cards.s.ScuteSwarm;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientGreenwarden.class, Forest.class, GrizzlyBears.class, HornOfGreed.class,
        TatyovaBenthicDruid.class, AshayaSoulOfTheWild.class, KazanduMammoth.class,
        ScuteSwarm.class, TurnToFrog.class})
class AncientGreenwardenTest extends BaseCardTest {

    @Test
    void controllerMayPlayLandsFromGraveyard() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void doublesLandfallTriggerFromLandEnteringUnderControllerControl() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDoubleLandPlayTriggers() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        // Horn of Greed triggers on playing a land, rather than a land entering.
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void twoGreenwardensAddTwoTriggersRatherThanMultiplying() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(3);
    }

    @Test
    void doesNotDoubleOpponentsLandfall() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player2, new ScuteSwarm());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void playingGraveyardLandUsesNormalLandAllowance() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayPlayNonlandFrontModalCardsLandFaceFromGraveyard() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.setGraveyard(player1, List.of(new KazanduMammoth()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(), true);

        harness.assertOnBattlefield(player1, "Kazandu Valley");
        assertThat(findPermanent(player1, "Kazandu Valley").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doublesLandfallForCreatureEnteringAsLandDueToAshaya() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new KazanduMammoth(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void doesNotDoubleLandfallAfterLosingAbilities() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Ancient Greenwarden").getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }
}
