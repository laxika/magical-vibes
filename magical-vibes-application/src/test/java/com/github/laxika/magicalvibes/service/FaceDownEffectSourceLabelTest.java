package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessBoostEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A floating effect made by an ability of a face-down permanent is shown as coming from a face-down card, not from the
 * hidden card (CR 708.2, 708.5): the label reaches every player through the affected permanent's modifier lines.
 */
@CardUsed({ExaltedAngel.class, GrizzlyBears.class})
class FaceDownEffectSourceLabelTest extends BaseCardTest {

    private static final String HIDDEN_NAME = "Exalted Angel";

    @Test
    @DisplayName("An effect from a face-down permanent's ability isn't labelled with the hidden card's name")
    void faceDownSourceIsNotNamedInEffectLabel() {
        Permanent hidden = addCreatureReady(player1, new ExaltedAngel());
        hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        resolveBoostFrom(hidden, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        harness.publishState();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains(HIDDEN_NAME));
        assertThat(harness.getConn2().getSentMessages()).isNotEmpty()
                .allSatisfy(message -> assertThat(message).doesNotContain(HIDDEN_NAME));
        assertThat(gd.floatingEffects).extracting(FloatingContinuousEffect::sourceCardName)
                .noneMatch(HIDDEN_NAME::equals);
    }

    @Test
    @DisplayName("An effect from a face-up permanent's ability keeps the card's name as its label")
    void faceUpSourceIsNamedInEffectLabel() {
        Permanent angel = addCreatureReady(player1, new ExaltedAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        resolveBoostFrom(angel, bears);

        assertThat(gd.floatingEffects).extracting(FloatingContinuousEffect::sourceCardName).contains(HIDDEN_NAME);
    }

    private void resolveBoostFrom(Permanent source, Permanent target) {
        List<CardEffect> effects = new ArrayList<>(List.of(new PowerToughnessBoostEffect(1, 1)));
        gd.stack.add(new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(), player1.getId(),
                "ability", effects, target.getId(), source.getId()));
        harness.clearMessages();
        harness.passBothPriorities();
    }
}
