package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywayRobber.class, Bonesplitter.class, GrizzlyBears.class})
class SkywayRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Escape tracks five other cards and offers an exiled artifact for free on combat damage")
    void escapeTracksCardsAndOffersExiledArtifact() {
        Bonesplitter bonesplitter = new Bonesplitter();
        List<Card> exiledForEscape = List.of(
                bonesplitter, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        SkywayRobber robber = new SkywayRobber();
        harness.setGraveyard(player1, List.of(robber, exiledForEscape.get(0), exiledForEscape.get(1),
                exiledForEscape.get(2), exiledForEscape.get(3), exiledForEscape.get(4)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escapedRobber = findPermanent(player1, "Skyway Robber");
        assertThat(gd.getCardsExiledByPermanent(escapedRobber.getId())).containsExactlyInAnyOrderElementsOf(exiledForEscape);

        escapedRobber.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities)
                .extracting(PendingMayAbility::targetCardId)
                .containsExactly(bonesplitter.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("A Skyway Robber that did not escape has no combat-damage ability")
    void ordinarySkywayRobberDoesNotGainAbility() {
        Permanent robber = harness.addToBattlefieldAndReturn(player1, new SkywayRobber());
        robber.setSummoningSick(false);
        gd.addToExile(player1.getId(), new Bonesplitter(), robber.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
