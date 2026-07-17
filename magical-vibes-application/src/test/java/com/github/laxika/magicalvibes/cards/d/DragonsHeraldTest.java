package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        harness.addToBattlefield(player1, new DragonsHerald());
        Permanent herald = findPermanent(player1, "Dragon's Herald");
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 3);
        return herald;
    }

    private Card hellkiteOverlord() {
        Card card = new Card() {};
        card.setName("Hellkite Overlord");
        card.setType(CardType.CREATURE);
        card.setPower(8);
        card.setToughness(8);
        return card;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new ScatheZombies()); // black
        harness.addToBattlefield(player1, new HillGiant());      // red
        // No green creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Black sacrifice prompt only offers black creatures")
    void blackPromptOffersOnlyBlackCreatures() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new ScatheZombies()).getId();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(zombiesId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one black, one red, and one green creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new ScatheZombies()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        // Black pick, then red pick; the sole remaining green creature is paid automatically.
        harness.handlePermanentChosen(player1, zombiesId);
        harness.handlePermanentChosen(player1, giantId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"))
                .anyMatch(c -> c.getName().equals("Hill Giant"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // The Herald itself was not sacrificed and the ability is on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon's Herald"));
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Hellkite Overlord by name and puts it onto the battlefield")
    void resolvingPutsHellkiteOverlordOntoBattlefield() {
        setUpHerald();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new ScatheZombies()).getId();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(hellkiteOverlord(), new ScatheZombies()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombiesId);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Hellkite Overlord"));

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hellkite Overlord"));
    }
}
