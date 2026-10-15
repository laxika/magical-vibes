package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IanMalcolmChaotician.class, Divination.class, Forest.class, GoblinPiker.class, TurnToFrog.class})
class IanMalcolmChaoticianTest extends BaseCardTest {

    @Test
    void controllersSecondDrawExilesTheirLibraryTopAndOpponentCanCastItWithAnyMana() {
        Permanent ian = harness.addToBattlefieldAndReturn(player1, new IanMalcolmChaotician());
        Card exiledSpell = new GoblinPiker();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), exiledSpell));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledSpell.getId()).sourcePermanentId()).isEqualTo(ian.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castFromExile(player2, exiledSpell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Piker");
    }

    @Test
    void opponentSecondDrawExilesTheirTopCardButTheyCannotCastTheirOwnExile() {
        harness.addToBattlefield(player1, new IanMalcolmChaotician());
        Card exiledCard = new GoblinPiker();
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), exiledCard));
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId()).sourcePermanentId()).isNotNull();
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player2, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAllAbilitiesRemovesPermissionToCastPreviouslyExiledCards() {
        Permanent ian = harness.addToBattlefieldAndReturn(player1, new IanMalcolmChaotician());
        Card exiledSpell = exileOnSecondDraw(new GoblinPiker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, ian.getId());

        assertThat(gqs.hasLostAllAbilities(gd, ian)).isTrue();
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player2, exiledSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledSpell.getId())).isNotNull();
    }

    @Test
    void permissionDoesNotAllowCastingCreatureOutsideMainPhase() {
        harness.addToBattlefield(player1, new IanMalcolmChaotician());
        Card exiledSpell = exileOnSecondDraw(new GoblinPiker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiledSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledSpell.getId())).isNotNull();
    }

    @Test
    void nonactivePlayerCannotCastAnExiledInstant() {
        Permanent ian = harness.addToBattlefieldAndReturn(player1, new IanMalcolmChaotician());
        Card exiledInstant = exileOnSecondDraw(new TurnToFrog());
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiledInstant.getId(), ian.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledInstant.getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, exiledInstant.getId(), ian.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledInstant.getId())).isNull();
        assertThat(gqs.hasLostAllAbilities(gd, ian)).isTrue();
    }

    @Test
    void permissionDoesNotAllowPlayingExiledLand() {
        harness.addToBattlefield(player1, new IanMalcolmChaotician());
        Card exiledLand = exileOnSecondDraw(new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player2, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledLand.getId())).isNotNull();
    }

    @Test
    void thirdAndFourthDrawsDoNotExileMoreCards() {
        harness.addToBattlefield(player1, new IanMalcolmChaotician());
        Card exiledSpell = exileOnSecondDraw(new GoblinPiker());
        Card remainingTop = new GoblinPiker();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), remainingTop));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingTop);
        assertThat(gd.findExiledCard(exiledSpell.getId())).isNotNull();
        assertThat(gd.findExiledCard(remainingTop.getId())).isNull();
    }

    private Card exileOnSecondDraw(Card card) {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), card));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        return card;
    }
}
