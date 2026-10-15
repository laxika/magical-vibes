package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalrandSkySummoner.class, SearingSpear.class, Divination.class, TimberpackWolf.class})
class TalrandSkySummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 2/2 flying Drake token")
    void instantCreatesDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
        Permanent drake = findPermanent(player1, "Drake");
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a sorcery creates a Drake token")
    void sorceryCreatesDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Drake token")
    void creatureSpellCreatesNoDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new TimberpackWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isZero();
    }

    @Test
    @DisplayName("The Drake is created before the triggering spell resolves")
    void drakeResolvesBeforeSpell() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drake")).isZero();
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Drake");
        assertThat(drake).isNotNull();
        assertThat(drake.getCard().isToken()).isTrue();
        assertThat(drake.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(drake.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(drake.getCard().getSubtypes()).containsExactly(CardSubtype.DRAKE);
        assertThat(drake.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Talrand")
    void opponentSpellCreatesNoDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Drake")).isZero();
        assertThat(countPermanents(player2, "Drake")).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The trigger still creates a Drake if Talrand dies in response")
    void triggerSurvivesTalrandRemoval() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Talrand, Sky Summoner"));
        harness.assertNotOnBattlefield(player1, "Talrand, Sky Summoner");
        harness.assertInGraveyard(player1, "Talrand, Sky Summoner");
        assertThat(countPermanents(player1, "Drake")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
        assertThat(countPermanents(player2, "Drake")).isZero();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each instant cast in the same turn creates another Drake")
    void multipleSpellsCreateMultipleDrakes() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new SearingSpear(), new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(2);
        harness.assertLife(player2, 14);
    }
}
