package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DerangedHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates four Squirrels that get +1/+1")
    void enteringCreatesFourBuffedSquirrels() {
        castAndResolveHermit();

        List<Permanent> squirrels = squirrelTokens(player1);
        assertThat(squirrels).hasSize(4);
        for (Permanent squirrel : squirrels) {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The static ability buffs Squirrels controlled by any player")
    void buffsSquirrelsControlledByAnyPlayer() {
        harness.addToBattlefield(player1, new DerangedHermit());
        Permanent ownSquirrel = harness.addToBattlefieldAndReturn(player1, squirrelToken());
        Permanent opponentSquirrel = harness.addToBattlefieldAndReturn(player2, squirrelToken());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining echo sacrifices Deranged Hermit at the next upkeep")
    void decliningEchoSacrificesHermit() {
        castAndResolveHermit();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Deranged Hermit");
        harness.assertInGraveyard(player1, "Deranged Hermit");
    }

    @Test
    @DisplayName("Paying echo keeps Deranged Hermit and echo is one-shot")
    void payingEchoKeepsHermitAndIsOneShot() {
        castAndResolveHermit();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Deranged Hermit");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Deranged Hermit");
    }

    private void castAndResolveHermit() {
        harness.setHand(player1, List.of(new DerangedHermit()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deranged Hermit");
    }

    private List<Permanent> squirrelTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();
    }

    private Card squirrelToken() {
        Card card = new Card();
        card.setName("Squirrel");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SQUIRREL));
        card.setToken(true);
        return card;
    }
}
