package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodfireEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have first strike or trample without both card types in the graveyard")
    void doesNotHaveKeywordsWithoutInstantAndSorcery() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new BloodfireEnforcers());

        Permanent enforcers = findEnforcers();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has first strike and trample with an instant and a sorcery in the graveyard")
    void hasKeywordsWithInstantAndSorcery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new BloodfireEnforcers());

        Permanent enforcers = findEnforcers();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's graveyard does not satisfy the condition")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new BloodfireEnforcers());

        Permanent enforcers = findEnforcers();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses first strike and trample when either card type leaves the graveyard")
    void losesKeywordsWhenConditionStopsBeingMet() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new BloodfireEnforcers());

        Permanent enforcers = findEnforcers();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new Shock()));

        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enforcers, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent findEnforcers() {
        return findPermanent(player1, "Bloodfire Enforcers");
    }
}
