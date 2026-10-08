package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantFarewell.class, DoomBlade.class, GrizzlyBears.class, Mountain.class, ActOfTreason.class})
class ValiantFarewellTest extends BaseCardTest {

    @Test
    void pumpsDrawsAndRewardsTheNextCreatureAfterTargetLeaves() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), new DoomBlade(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent rewardedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, rewardedCreature)).isEqualTo(4);
    }

    @Test
    void onlyRewardsOneCreatureSpell() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), new DoomBlade(), firstCreature, secondCreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, firstCreature))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, secondCreature))).isEqualTo(2);
    }

    @Test
    void boonBelongsToFarewellCasterAfterCreatureChangesControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ActOfTreason(), new DoomBlade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castSorcery(player2, 0, target.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, nextCreature))).isEqualTo(4);
    }

    @Test
    void doesNotDrawOrCreateBoonWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCreature);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, nextCreature))).isEqualTo(2);
    }

    @Test
    void creatureCastBeforeTargetLeavesDoesNotReceiveBoost() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, nextCreature))).isEqualTo(2);
    }

    @Test
    void leavingOnLaterTurnDoesNotCreateBoon() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), new DoomBlade(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, nextCreature))).isEqualTo(2);
    }

    @Test
    void earnedBoonSurvivesUntilCreatureIsCastOnLaterTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears nextCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new ValiantFarewell(), new DoomBlade(), nextCreature));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, nextCreature))).isEqualTo(4);
    }

    private Permanent findPermanentByCard(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
