package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrossSkullbombTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and draws a card")
    void sacrificesItselfAndDraws() {
        Permanent skullbomb = addSkullbomb();
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(skullbomb), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Returns a targeted creature card and draws a card")
    void returnsCreatureAndDraws() {
        Permanent skullbomb = addSkullbomb();
        Card creature = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(skullbomb), 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(creature, draw);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreature() {
        Permanent skullbomb = addSkullbomb();
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(skullbomb), 1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skullbomb);
    }

    @Test
    @DisplayName("The creature return ability works only at sorcery speed")
    void creatureReturnIsSorcerySpeedOnly() {
        Permanent skullbomb = addSkullbomb();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(skullbomb), 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSkullbomb() {
        return harness.addToBattlefieldAndReturn(player1, new DrossSkullbomb());
    }

    private int battlefieldIndex(Permanent skullbomb) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(skullbomb);
    }
}
