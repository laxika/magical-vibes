package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeadOfTheHunt.class, DoomBlade.class, GrizzlyBears.class, WrathOfGod.class})
class HeadOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's dying creature and creates a 2/2 green Wolf")
    void exilesOpponentCreatureAndCreatesWolf() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);
        Permanent wolf = wolves.getFirst();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not replace the controller's own dying creature")
    void ownCreatureDiesNormally() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Creates one Wolf for each opponent creature exiled by a mass destruction")
    void createsOneWolfPerExiledCreature() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Grizzly Bears");
    }
}
