package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({TheSpearOfLeonidas.class, GrizzlyBears.class, Forest.class})
class TheSpearOfLeonidasTest extends BaseCardTest {

    private static final String BULL_RUSH =
            "Bull Rush — It gains double strike until end of turn";
    private static final String SUMMON =
            "Summon — Create Phobos, a legendary 3/2 red Horse creature token";
    private static final String REVELATION =
            "Revelation — Discard two cards, then draw two cards";

    @Test
    @DisplayName("Equip {2} attaches the Spear to a creature you control")
    void equipsToCreatureYouControl() {
        Permanent spear = addSpearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Bull Rush grants double strike to the equipped creature until end of turn")
    void bullRushGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = addSpearReady(player1);
        spear.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleListChoice(player1, BULL_RUSH));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Summon creates a legendary 3/2 red Horse token named Phobos")
    void summonCreatesPhobos() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = addSpearReady(player1);
        spear.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, SUMMON);
        harness.passBothPriorities();

        Permanent phobos = findPermanent(player1, "Phobos");
        assertThat(gqs.getEffectivePower(gd, phobos)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, phobos)).isEqualTo(2);
        assertThat(phobos.getCard().getSubtypes()).containsExactly(CardSubtype.HORSE);
        assertThat(phobos.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(phobos.getCard().getColors()).containsExactly(com.github.laxika.magicalvibes.model.CardColor.RED);
    }

    @Test
    @DisplayName("Revelation discards two cards, then draws two cards")
    void revelationDiscardsThenDraws() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = addSpearReady(player1);
        spear.setAttachedTo(creature.getId());
        Card discardedOne = new GrizzlyBears();
        Card discardedTwo = new GrizzlyBears();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discardedOne, discardedTwo)));
        harness.setLibrary(player1, new ArrayList<>(List.of(drawnOne, drawnTwo)));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REVELATION);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(discardedOne, discardedTwo);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }

    private Permanent addSpearReady(Player player) {
        Permanent spear = new Permanent(new TheSpearOfLeonidas());
        spear.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spear);
        return spear;
    }
}
