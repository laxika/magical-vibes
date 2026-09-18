package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, KrosanVerge.class, SuntailHawk.class, Unsummon.class, WormfangDrake.class})
class WormfangDrakeTest extends BaseCardTest {

    private void castWormfangDrake() {
        harness.setHand(player1, List.of(new WormfangDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself when its controller has no other creature")
    void sacrificesItselfWithoutAnotherCreature() {
        castWormfangDrake();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertInGraveyard(player1, "Wormfang Drake");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifices itself when the only other creature is controlled by an opponent")
    void sacrificesItselfWithoutCreatureItControls() {
        harness.addToBattlefield(player2, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertInGraveyard(player1, "Wormfang Drake");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles another creature it controls")
    void exilesAnotherCreature() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hawk.getId());

        harness.assertOnBattlefield(player1, "Wormfang Drake");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Returns the exiled creature when it leaves the battlefield")
    void returnsExiledCreatureWhenItLeaves() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, hawk.getId());

        Permanent drake = findPermanent(player1, "Wormfang Drake");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, drake));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Exiles another creature it controls")
    void exilesAnotherCreatureJudReview() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWormfangDrake();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Wormfang Drake");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Wormfang Drake"), Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can champion only another creature controlled by its controller")
    void canChampionOnlyAnotherCreatureControllerControls() {
        var eligibleCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var nonCreature = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        var opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWormfangDrake();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligibleCreature.getId())
                .doesNotContain(nonCreature.getId(), opponentCreature.getId(),
                        harness.getPermanentId(player1, "Wormfang Drake"));

        harness.handlePermanentChosen(player1, eligibleCreature.getId());

        harness.assertOnBattlefield(player1, "Wormfang Drake");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's battlefield")
    void returnsControlledCreatureToItsOwnersBattlefield() {
        GrizzlyBears ownedByPlayer2 = new GrizzlyBears();
        ownedByPlayer2.setOwnerId(player2.getId());
        var stolenCreature = harness.addToBattlefieldAndReturn(player2, ownedByPlayer2);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), stolenCreature,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test setup"));

        castWormfangDrake();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, stolenCreature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID drakeId = harness.getPermanentId(player1, "Wormfang Drake");
        harness.castInstant(player1, 0, drakeId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ownedByPlayer2.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownedByPlayer2.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(ownedByPlayer2.getId()));
    }
}
