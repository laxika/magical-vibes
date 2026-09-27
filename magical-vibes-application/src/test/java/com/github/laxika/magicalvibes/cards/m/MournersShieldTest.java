package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.v.VulshokBerserker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MournersShield.class, VulshokBerserker.class, FangrenHunter.class, AlphaMyr.class,
        MoltenRain.class})
class MournersShieldTest extends BaseCardTest {

    @Test
    @DisplayName("May exile and imprint a card from a graveyard when it enters")
    void imprintsCardFromGraveyard() {
        Card imprinted = new VulshokBerserker();

        Permanent shield = castShield(imprinted, true);

        assertThat(gd.getImprintedCard(shield.getCard())).isSameAs(imprinted);
        assertThat(gd.getCardsExiledByPermanent(shield.getId())).containsExactly(imprinted);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(imprinted);
    }

    @Test
    @DisplayName("Only sources sharing a color with the imprinted card can be chosen")
    void restrictsSourceChoiceToImprintedCardColor() {
        Permanent shield = castShield(new VulshokBerserker(), true);
        Permanent redSource = addCreatureReady(player2, new VulshokBerserker());
        Permanent greenSource = addCreatureReady(player2, new FangrenHunter());
        Permanent colorlessSource = addCreatureReady(player2, new AlphaMyr());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int shieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shield);
        harness.activateAbility(player1, shieldIndex, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(redSource.getId()).doesNotContain(greenSource.getId());
        assertThat(choice.validIds()).doesNotContain(colorlessSource.getId());
    }

    @Test
    @DisplayName("Prevents all damage from the chosen source for the turn")
    void preventsDamageFromChosenSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castShield(new VulshokBerserker(), true);
        Permanent redSource = addCreatureReady(player2, new VulshokBerserker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        redSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can imprint a noncreature card from a graveyard")
    void imprintsNoncreatureCardFromGraveyard() {
        Card imprinted = new MoltenRain();

        Permanent shield = castShield(imprinted, true);

        assertThat(gd.getImprintedCard(shield.getCard())).isSameAs(imprinted);
        assertThat(gd.getCardsExiledByPermanent(shield.getId())).containsExactly(imprinted);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline to exile the targeted card")
    void mayDeclineToImprintCard() {
        Card imprinted = new VulshokBerserker();

        Permanent shield = castShield(imprinted, false);

        assertThat(gd.getImprintedCard(shield.getCard())).isNull();
        assertThat(gd.getCardsExiledByPermanent(shield.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(imprinted);
    }

    @Test
    @DisplayName("Prevents damage only from the chosen source")
    void preventsDamageOnlyFromChosenSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castShield(new VulshokBerserker(), true);
        Permanent chosenSource = addCreatureReady(player2, new VulshokBerserker());
        Permanent otherSource = addCreatureReady(player2, new VulshokBerserker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        castShield(new VulshokBerserker(), true);
        Permanent redSource = addCreatureReady(player2, new VulshokBerserker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(redSource.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(redSource.getId());
    }

    @Test
    @DisplayName("Cannot choose a source before a card has been imprinted")
    void cannotChooseSourceWithoutImprintedCard() {
        harness.setLife(player1, 20);
        castShield(new VulshokBerserker(), false);
        Permanent redSource = addCreatureReady(player2, new VulshokBerserker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();

        redSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }

    private Permanent castShield(Card imprintedCard, boolean accept) {
        harness.setGraveyard(player2, List.of(imprintedCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MournersShield()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(imprintedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);

        return findPermanent(player1, "Mourner's Shield");
    }
}
