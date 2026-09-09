package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistReality.class, GrizzlyBears.class, Forest.class})
class TwistRealityTest extends BaseCardTest {

    @Test
    void countersTargetSpellWithFirstMode() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new TwistReality()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Twist Reality");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsNonSpellTargetForCounterMode() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TwistReality()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestsOneOfTopTwoAndPutsTheOtherInGraveyardWithSecondMode() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new TwistReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        harness.assertInGraveyard(player1, "Twist Reality");
    }
}
