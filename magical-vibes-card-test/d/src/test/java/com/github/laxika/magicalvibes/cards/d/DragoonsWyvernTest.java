package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DragoonsWyvern.class)
class DragoonsWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("When Dragoon's Wyvern enters, it creates a 1/1 colorless Hero token")
    void entersCreatesHeroToken() {
        harness.castFromHand(player1, new DragoonsWyvern(), "{2}{U}");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Hero");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HERO);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast creates exactly one Hero for the entering creature's controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new DragoonsWyvern());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Hero")).isEqualTo(1);
        assertThat(countPermanents(player1, "Hero")).isZero();
        Permanent token = findPermanent(player2, "Hero");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The Hero trigger resolves even if the Wyvern dies before resolution")
    void tokenTriggerSurvivesSourceDeath() {
        Permanent wyvern = harness.enterBattlefieldAndReturn(player1, new DragoonsWyvern());
        assertThat(countPermanents(player1, "Hero")).isZero();
        wyvern.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wyvern);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(countPermanents(player2, "Hero")).isZero();
    }
}
