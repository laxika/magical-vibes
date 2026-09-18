package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, KrosanVerge.class, RiftstonePortal.class, SuntailHawk.class, Threaten.class, WorldgorgerDragon.class})
class WorldgorgerDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles all other permanents controlled by its controller")
    void etbExilesOtherPermanentsYouControl() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());

        castAndResolveWorldgorgerDragon(player1);

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Worldgorger Dragon");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suntail Hawk"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles other noncreature permanents controlled by its controller")
    void etbExilesOtherNoncreaturePermanentsYouControl() {
        harness.addToBattlefield(player1, new KrosanVerge());

        castAndResolveWorldgorgerDragon(player1);

        harness.assertNotOnBattlefield(player1, "Krosan Verge");
        harness.assertOnBattlefield(player1, "Worldgorger Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Krosan Verge"));
    }

    @Test
    @DisplayName("Exiled permanents return under their owners' control when Worldgorger Dragon leaves")
    void exiledPermanentsReturnWhenDragonLeaves() {
        harness.addToBattlefield(player1, new SuntailHawk());
        castAndResolveWorldgorgerDragon(player1);

        var dragon = findPermanent(player1, "Worldgorger Dragon");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Worldgorger Dragon");
    }

    @Test
    @DisplayName("Exiled permanents return under their owners' control rather than their controller's")
    void exiledPermanentsReturnUnderTheirOwnersControl() {
        var hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), hawk,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test setup"));

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        castAndResolveWorldgorgerDragon(player2);

        var dragon = findPermanent(player2, "Worldgorger Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suntail Hawk"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));

        harness.assertOnBattlefield(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Permanents exiled after the source leaves remain exiled")
    void etbResolvedAfterDragonLeavesDoesNotCreateReturnLink() {
        harness.addToBattlefield(player1, new SuntailHawk());

        castWorldgorgerDragon(player1);
        harness.passBothPriorities();

        var dragon = findPermanent(player1, "Worldgorger Dragon");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Worldgorger Dragon");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suntail Hawk"));
    }

    private void castAndResolveWorldgorgerDragon(Player caster) {
        castWorldgorgerDragon(caster);
        resolveAllTriggers();
    }

    private void castWorldgorgerDragon(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(caster, new WorldgorgerDragon(), "{3}{R}{R}{R}");
    }

    @Test
    @DisplayName("ETB exiles noncreature permanents you control too")
    void etbExilesNoncreaturePermanentsYouControl() {
        harness.addToBattlefield(player1, new RiftstonePortal());

        castAndResolveWorldgorgerDragonForJudReview();

        harness.assertNotOnBattlefield(player1, "Riftstone Portal");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Riftstone Portal"));
    }

    @Test
    @DisplayName("A stolen permanent returns under its owner's control when Dragon leaves")
    void stolenPermanentReturnsUnderItsOwnersControl() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castThreatenForJudReview(bears);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        castAndResolveWorldgorgerDragonForJudReview();

        var dragon = findPermanent(player1, "Worldgorger Dragon");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void castAndResolveWorldgorgerDragonForJudReview() {
        castWorldgorgerDragonForJudReview();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castWorldgorgerDragonForJudReview() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WorldgorgerDragon(), "{3}{R}{R}{R}");
    }

    private void castThreatenForJudReview(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
