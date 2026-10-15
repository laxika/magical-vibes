package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClarionSpirit.class, LightningBolt.class})
class ClarionSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Spirit token for your second spell each turn")
    void createsTokenForSecondSpell() {
        addCreatureReady(player1, new ClarionSpirit());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts their second spell")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new ClarionSpirit());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Clarion Spirit cast as the first spell counts toward the second spell")
    void countsItsOwnCastBeforeEntering() {
        harness.setHand(player1, List.of(new ClarionSpirit(), new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Spirit");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Entering as the second spell does not trigger retroactively or on the third spell")
    void doesNotTriggerForItsOwnSecondSpellCast() {
        harness.setHand(player1, List.of(new LightningBolt(), new ClarionSpirit(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("The spell count resets and your second instant on an opponent's turn triggers")
    void triggersAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new ClarionSpirit());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("The token trigger resolves even if Clarion Spirit is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        Permanent clarion = harness.addToBattlefieldAndReturn(player1, new ClarionSpirit());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.castAndResolveInstant(player1, 0, clarion.getId());
        assertThat(countPermanents(player1, "Clarion Spirit")).isZero();

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }
}
