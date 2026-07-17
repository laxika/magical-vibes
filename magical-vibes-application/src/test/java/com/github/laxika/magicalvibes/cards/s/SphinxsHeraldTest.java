package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

class SphinxsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        harness.addToBattlefield(player1, new SphinxsHerald());
        Permanent herald = findPermanent(player1, "Sphinx's Herald");
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 3);
        return herald;
    }

    private Card sphinxSovereign() {
        Card card = new Card() {};
        card.setName("Sphinx Sovereign");
        card.setType(CardType.CREATURE);
        card.setPower(6);
        card.setToughness(6);
        return card;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new SuntailHawk());   // white
        harness.addToBattlefield(player1, new FugitiveWizard()); // blue
        // No black creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("White sacrifice prompt only offers white creatures")
    void whitePromptOffersOnlyWhiteCreatures() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(hawkId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one white, one blue, and one black creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        harness.activateAbility(player1, 0, null, null);
        // White pick, then blue pick; the sole remaining black creature is paid automatically.
        harness.handlePermanentChosen(player1, hawkId);
        harness.handlePermanentChosen(player1, wizardId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
        // The Herald itself was not sacrificed and the ability is on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sphinx's Herald"));
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Sphinx Sovereign by name and puts it onto the battlefield")
    void resolvingPutsSphinxSovereignOntoBattlefield() {
        setUpHerald();
        UUID hawkId = harness.addToBattlefieldAndReturn(player1, new SuntailHawk()).getId();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(sphinxSovereign(), new ScatheZombies()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, hawkId);
        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Sphinx Sovereign"));

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sphinx Sovereign"));
    }
}
