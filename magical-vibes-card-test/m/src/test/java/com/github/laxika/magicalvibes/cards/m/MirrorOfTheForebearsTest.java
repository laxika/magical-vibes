package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorOfTheForebears.class, GrizzlyBears.class, HillGiant.class})
class MirrorOfTheForebearsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Mirror enters records that type")
    void choosesCreatureTypeAsItEnters() {
        harness.setHand(player1, List.of(new MirrorOfTheForebears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Mirror of the Forebears").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Copies a creature of the chosen type and remains an artifact")
    void copiesChosenTypeCreatureAndRemainsArtifact() {
        Permanent mirror = addMirror(CardSubtype.BEAR);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mirror.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(mirror.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(mirror.getCard().getPower()).isEqualTo(2);
        assertThat(mirror.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy wears off at the end of the turn")
    void copyWearsOffAtEndOfTurn() {
        Permanent mirror = addMirror(CardSubtype.BEAR);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Mirror of the Forebears");
        assertThat(mirror.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(mirror.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature outside the chosen type or controlled by an opponent")
    void restrictsTargetsToChosenTypeYouControl() {
        Permanent mirror = addMirror(CardSubtype.BEAR);
        Permanent wrongType = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, wrongType.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mirror.getCard().getName()).isEqualTo("Mirror of the Forebears");
    }

    private Permanent addMirror(CardSubtype chosenSubtype) {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirrorOfTheForebears());
        mirror.setChosenSubtype(chosenSubtype);
        return mirror;
    }
}
