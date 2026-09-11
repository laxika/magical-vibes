package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatchworkBeastie.class, GrizzlyBears.class, Forest.class, Shock.class,
        Divination.class, Pacifism.class, LeoninScimitar.class})
class PatchworkBeastieTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without delirium")
    void cannotAttackWithoutDelirium() {
        addReadyBeastie(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with delirium")
    void canAttackWithDelirium() {
        harness.setLife(player2, 20);
        addReadyBeastie(player1, fourCardTypes());

        declareAttackers(player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block without delirium")
    void cannotBlockWithoutDelirium() {
        addReadyBeastie(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with delirium")
    void canBlockWithDelirium() {
        Permanent beastie = addReadyBeastie(player1, fourCardTypes());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(beastie.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the upkeep trigger mills a card")
    void acceptingUpkeepTriggerMills() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new PatchworkBeastie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not mill")
    void decliningUpkeepTriggerDoesNotMill() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new PatchworkBeastie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyBeastie(Player player, List<Card> graveyard) {
        harness.setGraveyard(player, graveyard);
        return addCreatureReady(player, new PatchworkBeastie());
    }

    private List<Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Divination(),
                new Pacifism(), new LeoninScimitar());
    }
}
