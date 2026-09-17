package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefyGravity.class, BenevolentBodyguard.class, KrosanVerge.class})
class DefyGravityTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BenevolentBodyguard());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Benevolent Bodyguard");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bodyguard = findPermanent(player1, "Benevolent Bodyguard");
        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Target creature an opponent controls gains flying")
    void grantsFlyingToOpponentsCreature() {
        harness.addToBattlefield(player2, new BenevolentBodyguard());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Benevolent Bodyguard");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bodyguard = findPermanent(player2, "Benevolent Bodyguard");
        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flashback grants flying and exiles Defy Gravity")
    void flashbackGrantsFlyingAndExilesSpell() {
        harness.addToBattlefield(player1, new BenevolentBodyguard());
        harness.setGraveyard(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Benevolent Bodyguard");
        harness.castAndResolveFlashback(player1, 0, targetId);

        Permanent bodyguard = findPermanent(player1, "Benevolent Bodyguard");
        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Defy Gravity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Defy Gravity"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new BenevolentBodyguard());
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Krosan Verge");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
