package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MournersShieldTest extends BaseCardTest {

    @Test
    @DisplayName("May exile and imprint a card from a graveyard when it enters")
    void imprintsCardFromGraveyard() {
        Card imprinted = new FireElemental();

        Permanent shield = castShield(imprinted, true);

        assertThat(gd.getImprintedCard(shield.getCard())).isSameAs(imprinted);
        assertThat(gd.getCardsExiledByPermanent(shield.getId())).containsExactly(imprinted);
    }

    @Test
    @DisplayName("Only sources sharing a color with the imprinted card can be chosen")
    void restrictsSourceChoiceToImprintedCardColor() {
        Permanent shield = castShield(new FireElemental(), true);
        Permanent redSource = addCreature(player2, new FireElemental());
        Permanent greenSource = addCreature(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int shieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shield);
        harness.activateAbility(player1, shieldIndex, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(redSource.getId()).doesNotContain(greenSource.getId());
    }

    @Test
    @DisplayName("Prevents all damage from the chosen source for the turn")
    void preventsDamageFromChosenSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castShield(new FireElemental(), true);
        Permanent redSource = addCreature(player2, new FireElemental());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        redSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent castShield(Card imprintedCard, boolean accept) {
        harness.setGraveyard(player2, new ArrayList<>(List.of(imprintedCard)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MournersShield()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);

        return findPermanent(player1, "Mourner's Shield");
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
