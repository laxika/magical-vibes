package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InquisitivePuppet.class)
class InquisitivePuppetTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldTriggersScryOne() {
        harness.setHand(player1, List.of(new InquisitivePuppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void exilingItselfCreatesAWhiteHumanToken() {
        Permanent puppet = harness.addToBattlefieldAndReturn(player1, new InquisitivePuppet());
        puppet.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inquisitive Puppet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Inquisitive Puppet"));

        Permanent human = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Human"))
                .findFirst()
                .orElseThrow();
        assertThat(human.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    void canActivateWhileSummoningSickAndExilesBeforeResolution() {
        Permanent puppet = harness.addToBattlefieldAndReturn(player1, new InquisitivePuppet());
        puppet.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(puppet.getCard());
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Human");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getColor())
                .isEqualTo(CardColor.WHITE);
        harness.assertOnBattlefield(player1, "Human");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithAnEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new InquisitivePuppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inquisitive Puppet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void scryCanPutTheTopCardOnTheBottom() {
        InquisitivePuppet top = new InquisitivePuppet();
        InquisitivePuppet next = new InquisitivePuppet();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new InquisitivePuppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
