package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

class AngelsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        harness.addToBattlefield(player1, new AngelsHerald());
        Permanent herald = findPermanent(player1, "Angel's Herald");
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 3);
        return herald;
    }

    private Card empyrialArchangel() {
        Card card = new Card() {};
        card.setName("Empyrial Archangel");
        card.setType(CardType.CREATURE);
        card.setPower(5);
        card.setToughness(8);
        return card;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new GrizzlyBears());  // green
        harness.addToBattlefield(player1, new SuntailHawk());   // white
        // No blue creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Green sacrifice prompt only offers green creatures")
    void greenPromptOffersOnlyGreenCreatures() {
        setUpHerald();
        UUID grizzlyId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(grizzlyId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one green, one white, and one blue creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID grizzlyId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, null);
        // Green pick, then white pick; the sole remaining blue creature is paid automatically.
        harness.handlePermanentChosen(player1, grizzlyId);
        harness.handlePermanentChosen(player1, hawkId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
        // The Herald itself was not sacrificed and the ability is on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Herald"));
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Empyrial Archangel by name and puts it onto the battlefield")
    void resolvingPutsEmpyrialArchangelOntoBattlefield() {
        setUpHerald();
        UUID grizzlyId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(empyrialArchangel(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, grizzlyId);
        harness.handlePermanentChosen(player1, hawkId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Empyrial Archangel"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Empyrial Archangel"));
    }
}
