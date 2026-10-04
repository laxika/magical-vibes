package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EssenceOfAntiquity.class})
class EssenceOfAntiquityTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up gives your creatures hexproof and untaps them")
    void turningFaceUpGrantsHexproofAndUntapsControlledCreatures() {
        Permanent ally = addCreatureReady(player1, new EssenceOfAntiquity());
        Permanent opponent = addCreatureReady(player2, new EssenceOfAntiquity());
        ally.tap();
        opponent.tap();

        Permanent essence = castFaceDown();
        essence.tap();
        turnFaceUp(essence);
        resolveAllTriggers();

        assertThat(essence.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, essence, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HEXPROOF)).isTrue();
        assertThat(essence.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void hexproofExpiresAtEndOfTurn() {
        Permanent ally = addCreatureReady(player1, new EssenceOfAntiquity());
        Permanent essence = castFaceDown();
        turnFaceUp(essence);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, essence, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HEXPROOF)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, essence, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void triggerAffectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        Permanent essence = castFaceDown();
        essence.tap();
        turnFaceUp(essence);

        assertThat(essence.isFaceDown()).isFalse();
        assertThat(essence.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, essence, Keyword.HEXPROOF)).isFalse();

        Permanent beforeResolution = addCreatureReady(player1, new EssenceOfAntiquity());
        beforeResolution.tap();
        resolveAllTriggers();
        assertThat(beforeResolution.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HEXPROOF)).isTrue();

        Permanent afterResolution = addCreatureReady(player1, new EssenceOfAntiquity());
        afterResolution.tap();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HEXPROOF)).isFalse();
        assertThat(afterResolution.isTapped()).isTrue();
    }

    @Test
    void castingFaceUpDoesNotTriggerHexproofOrUntap() {
        Permanent ally = addCreatureReady(player1, new EssenceOfAntiquity());
        ally.tap();
        harness.setHand(player1, List.of(new EssenceOfAntiquity()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(ally.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HEXPROOF)).isFalse();
        assertThat(findPermanents(player1, "Essence of Antiquity"))
                .hasSize(2)
                .allSatisfy(p -> assertThat(gqs.hasKeyword(gd, p, Keyword.HEXPROOF)).isFalse());
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new EssenceOfAntiquity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
    }

    private void turnFaceUp(Permanent essence) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(essence));
    }
}
