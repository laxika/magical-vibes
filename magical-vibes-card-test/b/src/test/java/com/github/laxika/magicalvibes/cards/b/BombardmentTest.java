package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bombardment.class, GrizzlyBears.class, Mountain.class, Quicken.class, Shock.class})
class BombardmentTest extends BaseCardTest {

    @Test
    void turnsAHandCardIntoAMissileThatDealsTwoDamage() {
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(new Bombardment(), card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void restoresCardsAtCleanup() {
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(new Bombardment(), card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Missile");

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isSameAs(card);
    }

    @Test
    void missileReturnsToItsOriginalCharacteristicsWhenItResolvesIntoTheGraveyard() {
        harness.setHand(player1, List.of(new Bombardment(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Missile");
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void drawingAnAffectedLibraryCardRestoresItBeforeCleanup() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Bombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(DrawService.class)
                .resolveDrawCard(gd, player1.getId()));

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void shufflingRestoresAffectedLibraryCardsBeforeCleanup() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Bombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).allSatisfy(card ->
                assertThat(card.getName()).isEqualTo("Missile"));
        harness.inMutationScope(() -> LibraryShuffleHelper.shuffleLibrary(gd, player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).allSatisfy(card ->
                assertThat(card.getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void transformedBasicLandRetainsItsBasicSupertype() {
        harness.setHand(player1, List.of(new Bombardment(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        Card missile = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(missile.getName()).isEqualTo("Missile");
        assertThat(missile.getSupertypes()).contains(CardSupertype.BASIC);
    }

    @Test
    void missileCanDestroyAnOpposingCreatureWithoutChangingBattlefieldCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bombardment(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void spellAlreadyOnTheStackCannotReuseItsOriginalTargetAsAMissile() {
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new Quicken(), new Shock(), new Bombardment()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }
}
