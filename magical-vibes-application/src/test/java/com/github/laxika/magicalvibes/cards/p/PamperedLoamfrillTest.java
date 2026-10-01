package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PamperedLoamfrill.class, GrizzlyBears.class, Forest.class})
class PamperedLoamfrillTest extends BaseCardTest {

    @Test
    void renewExilesSourceAndConjuresAnotherCreatureToLibraryTop() {
        Card source = new PamperedLoamfrill();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(source, target));
        harness.setLibrary(player1, List.of(new Forest()));
        readyRenew();

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        Card duplicate = gd.playerDecks.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void conjuredDuplicateKeepsItsPerpetualBoostAndDeathtouch() {
        Card source = new PamperedLoamfrill();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(source, target));
        harness.setLibrary(player1, List.of(new Forest()));
        readyRenew();

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        Card duplicate = gd.playerDecks.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(duplicate.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void renewCannotTargetTheSourceOrANoncreatureCard() {
        Card source = new PamperedLoamfrill();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(source, forest));
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void renewIsSorcerySpeedOnly() {
        Card source = new PamperedLoamfrill();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(source, target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyRenew() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
