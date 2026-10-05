package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BorealOutrider;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ToskiBearerOfSecrets;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoisonTheCup.class, Forest.class, BorealOutrider.class, Island.class, ToskiBearerOfSecrets.class})
class PoisonTheCupTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealOutrider());
        harness.setHand(player1, List.of(new PoisonTheCup()));
        addSpellMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Boreal Outrider");
        harness.assertInGraveyard(player2, "Boreal Outrider");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void foretoldSpellScriesTwoAfterDestroyingTargetCreature() {
        Card topCard = new Forest();
        Card secondCard = new Island();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealOutrider());
        PoisonTheCup spell = new PoisonTheCup();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        addSpellMana();
        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boreal Outrider");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Poison the Cup");
    }

    @Test
    void rejectsNoncreatureTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PoisonTheCup()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealOutrider());
        PoisonTheCup spell = new PoisonTheCup();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        addSpellMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new PoisonTheCup()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");

        harness.assertInHand(player1, "Poison the Cup");
    }

    @Test
    void foretoldSpellCanDestroyOwnCreatureOnOpponentsTurnWithOneGenericAndOneBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorealOutrider());
        PoisonTheCup spell = foretellForLaterTurn();
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of());

        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boreal Outrider");
        harness.assertInGraveyard(player1, "Boreal Outrider");
        harness.assertInGraveyard(player1, "Poison the Cup");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretoldSpellDoesNotScryWhenItsOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealOutrider());
        PoisonTheCup spell = foretellForLaterTurn();
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boreal Outrider");
        harness.assertInGraveyard(player1, "Poison the Cup");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretoldSpellStillScriesWhenIndestructibleTargetSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ToskiBearerOfSecrets());
        PoisonTheCup spell = foretellForLaterTurn();
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card thirdCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));

        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Toski, Bearer of Secrets");
        harness.assertNotInGraveyard(player2, "Toski, Bearer of Secrets");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, thirdCard, topCard);
        harness.assertInGraveyard(player1, "Poison the Cup");
        assertThat(gd.stack).isEmpty();
    }

    private PoisonTheCup foretellForLaterTurn() {
        PoisonTheCup spell = new PoisonTheCup();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return spell;
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
