package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DragonsRageChanneler.class, EnchantedEvening.class, Forest.class, GrizzlyBears.class, Shock.class})
class DragonsRageChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when its controller casts a noncreature spell")
    void surveilsWhenControllerCastsNoncreatureSpell() {
        addCreatureReady(player1, new DragonsRageChanneler());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil when its controller casts a creature spell")
    void doesNotSurveilForCreatureSpell() {
        addCreatureReady(player1, new DragonsRageChanneler());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Delirium gives it +2/+2, flying, and a must-attack requirement")
    void deliriumGrantsBoostFlyingAndMustAttack() {
        Permanent channeler = addCreatureReady(player1, new DragonsRageChanneler());
        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, channeler, Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new EnchantedEvening()));

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, channeler, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
