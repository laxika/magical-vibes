package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiveOfTheEyeTyrant.class, Mountain.class, GrizzlyBears.class})
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
        Permanent hive = addHiveReady(player1);

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
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    private void playHive() {
        harness.setHand(player1, List.of(new HiveOfTheEyeTyrant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addHiveReady(Player player) {
        Permanent hive = new Permanent(new HiveOfTheEyeTyrant());
        hive.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(hive);
        return hive;
    }

    private Permanent animateHive() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        return findHive();
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private Permanent findHive() {
        return findPermanent(player1, "Hive of the Eye Tyrant");
    }
}
