package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AugurIlVec;
import com.github.laxika.magicalvibes.cards.j.JudgeUnworthy;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.s.SamiteCenserBearer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosianRevivalist.class, SamiteCenserBearer.class, AugurIlVec.class, JudgeUnworthy.class,
        JhovallQueen.class})
class RamosianRevivalistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target Rebel permanent with mana value 5 or less from the graveyard")
    void returnsTargetRebelPermanent() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, revivalistIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Samite Censer-Bearer");
        harness.assertNotInGraveyard(player1, "Samite Censer-Bearer");
        assertThat(findPermanent(player1, "Ramosian Revivalist").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Rebel card")
    void rejectsNonRebelCard() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new AugurIlVec();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-permanent card")
    void rejectsNonPermanentCard() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new JudgeUnworthy();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a Rebel permanent in an opponent's graveyard")
    void rejectsOpponentsGraveyard() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires six mana to activate")
    void requiresSixMana() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires an untapped source")
    void requiresUntappedSource() {
        int revivalistIndex = addReadyRevivalist();
        findPermanent(player1, "Ramosian Revivalist").tap();
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a Rebel permanent with mana value greater than 5")
    void rejectsRebelWithTooHighManaValue() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new JhovallQueen();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void requiresSourceWithoutSummoningSickness() {
        harness.addToBattlefield(player1, new RamosianRevivalist());
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another Rebel when the target leaves the graveyard")
    void doesNotReplaceMissingTarget() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new SamiteCenserBearer();
        Card otherRebel = new RamosianRevivalist();
        harness.setGraveyard(player1, List.of(target, otherRebel));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, revivalistIndex, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Samite Censer-Bearer");
        harness.assertInGraveyard(player1, "Ramosian Revivalist");
        assertThat(countPermanents(player1, "Ramosian Revivalist")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("The ability resolves even after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        int revivalistIndex = addReadyRevivalist();
        Permanent source = findPermanent(player1, "Ramosian Revivalist");
        Card target = new SamiteCenserBearer();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, revivalistIndex, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Samite Censer-Bearer");
        harness.assertNotInGraveyard(player1, "Samite Censer-Bearer");
        harness.assertInGraveyard(player1, "Ramosian Revivalist");
        assertThat(findPermanent(player1, "Samite Censer-Bearer").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Samite Censer-Bearer").isSummoningSick()).isTrue();
    }
    private int addReadyRevivalist() {
        Permanent revivalist = addCreatureReady(player1, new RamosianRevivalist());
        return gd.playerBattlefields.get(player1.getId()).indexOf(revivalist);
    }
}
