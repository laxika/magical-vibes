package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterGallifreysEnd.class, CopperMyr.class, GrizzlyBears.class, Shock.class})
class TheMasterGallifreysEndTest extends BaseCardTest {

    @Test
    void opponentCanChooseLifeLoss() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();

        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
    }

    @Test
    void opponentCanChooseTokenCopy() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);

        Permanent copy = findPermanent(player1, "Copper Myr");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    void decliningLeavesTheArtifactCreatureInTheGraveyard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    void ignoresNonArtifactCreatureDeaths() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player2, bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void acceptExile() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
    }

    private void assertVillainousChoice() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION,
                TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
