package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AbundantMaw;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VexingScuttler.class, LightningBolt.class, Divination.class, GrizzlyBears.class, AbundantMaw.class})
class VexingScuttlerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns a targeted instant from the graveyard to hand")
    void castTriggerReturnsInstant() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    @DisplayName("Emerge cast trigger returns a targeted sorcery from the graveyard")
    void emergeCastTriggerReturnsSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInHand(player1, "Divination");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    @DisplayName("Does not trigger when the graveyard has no instant or sorcery card")
    void noTriggerForNonMatchingGraveyardCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vexing Scuttler");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineReturnWhenCastTriggerResolves() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    void cannotReturnCardFromOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    void targetLeavingGraveyardDoesNotPreventCreatureResolving() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    void emergeReductionCanRemoveAllGenericMana() {
        harness.addToBattlefield(player1, new AbundantMaw());
        UUID sacrificeId = harness.getPermanentId(player1, "Abundant Maw");
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abundant Maw");
        harness.assertInGraveyard(player1, "Abundant Maw");
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    void emergeReductionCannotRemoveBlueManaRequirement() {
        harness.addToBattlefield(player1, new AbundantMaw());
        UUID sacrificeId = harness.getPermanentId(player1, "Abundant Maw");
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Abundant Maw");
        harness.assertInHand(player1, "Vexing Scuttler");
    }
}
