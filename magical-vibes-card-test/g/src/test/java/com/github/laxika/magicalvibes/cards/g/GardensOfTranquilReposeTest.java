package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GardensOfTranquilRepose.class, com.github.laxika.magicalvibes.cards.g.GrizzlyBears.class, Shock.class})
class GardensOfTranquilReposeTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new GardensOfTranquilRepose(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void chaosCreatesOneDalekPlusOnePerCreatureExiledWithGardens() {
        gd.addToExile(player1.getId(), new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                source.getCard().getId());
        gd.addToExile(player1.getId(), new Shock(), source.getCard().getId());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Dalek", "Dalek", "Dalek");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(permanent -> assertThat(permanent.getCard().hasType(CardType.ARTIFACT)).isTrue());
    }

    @Test
    void creatureDeathExilesItAndItsControllerScries() {
        Permanent dying = harness.addToBattlefieldAndReturn(player2,
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Card dyingCard = dying.getCard();
        harness.setLibrary(player2, List.of(new Shock()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getCard().getId())).contains(dyingCard);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player2.getId());
    }
}
