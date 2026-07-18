package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class BehemothsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        harness.addToBattlefield(player1, new BehemothsHerald());
        Permanent herald = findPermanent(player1, "Behemoth's Herald");
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 3);
        return herald;
    }

    private Card godsire() {
        Card card = new Card() {};
        card.setName("Godsire");
        card.setType(CardType.CREATURE);
        card.setPower(8);
        card.setToughness(8);
        return card;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new HillGiant());     // red
        harness.addToBattlefield(player1, new GrizzlyBears());  // green
        // No white creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Red sacrifice prompt only offers red creatures")
    void redPromptOffersOnlyRedCreatures() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(giantId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one red, one green, and one white creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);
        // Red pick, then green pick; the sole remaining white creature is paid automatically.
        harness.handlePermanentChosen(player1, giantId);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        // The Herald itself was not sacrificed and the ability is on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Behemoth's Herald"));
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Godsire by name and puts it onto the battlefield")
    void resolvingPutsGodsireOntoBattlefield() {
        setUpHerald();
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(godsire(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, giantId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Godsire"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Godsire"));
    }
}
