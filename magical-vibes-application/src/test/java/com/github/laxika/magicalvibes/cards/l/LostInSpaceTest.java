package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInSpace.class, AngelicChorus.class, DarksteelCitadel.class, GrizzlyBears.class, Island.class})
class LostInSpaceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an artifact or creature on top, then surveils 1")
    void putsTargetOnTopThenSurveils() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetOwnerTopCard = new Island();
        Card surveilledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(targetOwnerTopCard));
        harness.setLibrary(player1, List.of(surveilledCard));

        cast(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), targetOwnerTopCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(surveilledCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lost in Space");
    }

    @Test
    @DisplayName("Allows an artifact land and its owner can put it on the bottom")
    void targetsArtifactLandAndPutsItOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Card targetOwnerTopCard = new Island();
        Card surveilledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(targetOwnerTopCard));
        harness.setLibrary(player1, List.of(surveilledCard));

        cast(target);

        harness.handleListChoice(player2, "Bottom");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetOwnerTopCard, target.getCard());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Darksteel Citadel");
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new LostInSpace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LostInSpace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
