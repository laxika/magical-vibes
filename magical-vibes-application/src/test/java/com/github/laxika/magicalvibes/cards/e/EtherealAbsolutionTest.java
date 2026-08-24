package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtherealAbsolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures get +1/+1 and opponents' creatures get -1/-1")
    void modifiesCreaturesByController() {
        harness.addToBattlefield(player1, new EtherealAbsolution());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles a noncreature card from an opponent's graveyard without creating a token")
    void exilesNoncreatureWithoutCreatingToken() {
        Permanent absolution = harness.addToBattlefieldAndReturn(player1, new EtherealAbsolution());
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(plains));
        addAbilityMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(absolution), 0,
                List.of(plains.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Plains");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(plains);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Exiling an opponent's creature card creates a white and black flying Spirit")
    void exilesCreatureAndCreatesSpirit() {
        Permanent absolution = harness.addToBattlefieldAndReturn(player1, new EtherealAbsolution());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addAbilityMana();

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(absolution), 0,
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getCard().getName()).isEqualTo("Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target a card in your own graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent absolution = harness.addToBattlefieldAndReturn(player1, new EtherealAbsolution());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(absolution), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
