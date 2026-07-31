package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasTheDeceiver;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaspOfTheBitterEndTest extends BaseCardTest {

    private void castBolasTargeting(Permanent target) {
        harness.setHand(player1, List.of(new NicolBolasTheDeceiver()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castPlaneswalker(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
    }

    @Test
    @DisplayName("Casting a Bolas planeswalker prompts a creature target for the trigger")
    void bolasSpellPromptsTarget() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NicolBolasTheDeceiver()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting sacrifice destroys the targeted creature and sacrifices the Wasp")
    void acceptSacrificesAndDestroys() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBolasTargeting(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertInGraveyard(player1, "Wasp of the Bitter End");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may keeps both creatures")
    void declineKeepsBoth() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBolasTargeting(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-Bolas spells do not trigger")
    void nonBolasSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WaspOfTheBitterEnd());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Wasp of the Bitter End");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
