package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DragToTheUnderworld;
import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RageScarredBerserker.class, NyxbornColossus.class, FinalDeath.class, DragToTheUnderworld.class})
class RageScarredBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature you control +1/+0 and indestructible")
    void etbBoostsTargetCreatureYouControlAndGrantsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new RageScarredBerserker()));
        addManaForBerserker();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        castResolve(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Can choose itself after entering an otherwise empty battlefield")
    void canTargetItself() {
        harness.setHand(player1, List.of(new RageScarredBerserker()));
        addManaForBerserker();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Rage-Scarred Berserker");
        harness.handlePermanentChosen(player1, berserker.getId());
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(1);
        assertThat(berserker.getToughnessModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves when the Berserker is exiled in response")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RageScarredBerserker());
        harness.setHand(player1, List.of(new RageScarredBerserker()));
        addManaForBerserker();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanents(player1, "Rage-Scarred Berserker").get(1);

        exileInResponse(source);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source.getCard());
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not affect the source when its target is exiled in response")
    void removedTargetDoesNotRedirectEffectsToSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RageScarredBerserker());
        harness.setHand(player1, List.of(new RageScarredBerserker()));
        addManaForBerserker();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        exileInResponse(target);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Rage-Scarred Berserker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(source.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted indestructible prevents destruction but does not prevent exile")
    void indestructiblePreventsDestructionButNotExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RageScarredBerserker());
        castResolve(target);
        harness.setHand(player2, List.of(new DragToTheUnderworld(), new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player2, "Drag to the Underworld");
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple ETB boosts stack on only the chosen creature")
    void multipleTriggersBoostOnlyTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        castResolve(target);
        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        for (Permanent source : findPermanents(player1, "Rage-Scarred Berserker")) {
            assertThat(source.getPowerModifier()).isZero();
            assertThat(source.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        }
        assertThat(gd.stack).isEmpty();
    }

    private void exileInResponse(Permanent target) {
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new RageScarredBerserker()));
        addManaForBerserker();
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void addManaForBerserker() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
