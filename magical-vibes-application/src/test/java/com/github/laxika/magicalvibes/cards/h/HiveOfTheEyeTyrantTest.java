package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SylvanAwakening;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HiveOfTheEyeTyrant.class, Mountain.class, HillGiantHerdgorger.class, SylvanAwakening.class})
class HiveOfTheEyeTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Hive of the Eye Tyrant enters tapped with two other lands")
    void entersTappedWithTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        playHive();

        assertThat(findHive().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hive of the Eye Tyrant enters untapped with fewer than two other lands")
    void entersUntappedWithFewerThanTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        playHive();

        assertThat(findHive().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Hive of the Eye Tyrant produces one black mana")
    void tappingProducesBlackMana() {
        Permanent hive = addCreatureReady(player1, new HiveOfTheEyeTyrant());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(hive.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hive of the Eye Tyrant becomes a 3/3 black Beholder creature with menace and stays a land")
    void animatesAsBlackBeholder() {
        Permanent hive = animateHive();

        assertThat(gqs.isCreature(gd, hive)).isTrue();
        assertThat(gqs.isLand(gd, hive)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hive)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hive)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, hive)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hive)).contains(CardSubtype.BEHOLDER);
        assertThat(gqs.hasKeyword(gd, hive, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with the animated Hive of the Eye Tyrant exiles a card from the defending player's graveyard")
    void attackingExilesDefendingPlayerGraveyardCard() {
        animateHive();
        Card bears = new HillGiantHerdgorger();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    void repeatedAnimationGrantsTwoSeparateExileTriggers() {
        animateHive();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Card first = new HillGiantHerdgorger();
        Card second = new Mountain();
        harness.setGraveyard(player2, List.of(first, second));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(first.getId(), second.getId());
    }

    @Test
    void animationByAnotherCardDoesNotGrantExileTrigger() {
        addCreatureReady(player1, new HiveOfTheEyeTyrant());
        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        Card card = new HillGiantHerdgorger();
        harness.setGraveyard(player2, List.of(card));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    void animationExpiresAtEndOfTurn() {
        Permanent hive = animateHive();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, hive)).isFalse();
        assertThat(gqs.isLand(gd, hive)).isTrue();
        assertThat(gqs.hasKeyword(gd, hive, Keyword.MENACE)).isFalse();
    }

    @Test
    void emptyDefendingGraveyardDoesNotAllowTargetingOwnGraveyard() {
        animateHive();
        Card card = new HillGiantHerdgorger();
        harness.setGraveyard(player1, List.of(card));
        harness.setGraveyard(player2, List.of());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    void opponentsLandsDoNotMakeHiveEnterTapped() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        playHive();

        assertThat(findHive().isTapped()).isFalse();
    }

    @Test
    void attackTriggerCanExileANoncreatureCard() {
        animateHive();
        Card card = new Mountain();
        harness.setGraveyard(player2, List.of(card));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    @Test
    void attackTriggerDoesNotRetargetWhenChosenCardLeavesGraveyard() {
        animateHive();
        Card target = new HillGiantHerdgorger();
        Card remaining = new Mountain();
        harness.setGraveyard(player2, List.of(target, remaining));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(target));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target, remaining);
    }

    private void playHive() {
        harness.setHand(player1, List.of(new HiveOfTheEyeTyrant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent animateHive() {
        addCreatureReady(player1, new HiveOfTheEyeTyrant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        return findHive();
    }

    private Permanent findHive() {
        return findPermanent(player1, "Hive of the Eye Tyrant");
    }
}
