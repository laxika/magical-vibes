package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

class DemonsHeraldTest extends BaseCardTest {

    private Permanent setUpHerald() {
        harness.addToBattlefield(player1, new DemonsHerald());
        Permanent herald = findPermanent(player1, "Demon's Herald");
        herald.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 3);
        return herald;
    }

    private Card princeOfThralls() {
        Card card = new Card() {};
        card.setName("Prince of Thralls");
        card.setType(CardType.CREATURE);
        card.setPower(8);
        card.setToughness(8);
        return card;
    }

    @Test
    @DisplayName("Cannot activate without a creature of each required color")
    void cannotActivateWithoutEachColor() {
        setUpHerald();
        harness.addToBattlefield(player1, new FugitiveWizard()); // blue
        harness.addToBattlefield(player1, new ScatheZombies());  // black
        // No red creature.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Blue sacrifice prompt only offers blue creatures")
    void bluePromptOffersOnlyBlueCreatures() {
        setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(wizardId);
    }

    @Test
    @DisplayName("Paying the cost sacrifices one blue, one black, and one red creature")
    void payingSacrificesOneOfEachColor() {
        setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new ScatheZombies()).getId();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        // Blue pick, then black pick; the sole remaining red creature is paid automatically.
        harness.handlePermanentChosen(player1, wizardId);
        harness.handlePermanentChosen(player1, zombiesId);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        // The Herald itself was not sacrificed and the ability is on the stack.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon's Herald"));
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving searches for Prince of Thralls by name and puts it onto the battlefield")
    void resolvingPutsPrinceOfThrallsOntoBattlefield() {
        setUpHerald();
        UUID wizardId = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()).getId();
        UUID zombiesId = harness.addToBattlefieldAndReturn(player1, new ScatheZombies()).getId();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(princeOfThralls(), new ScatheZombies()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, wizardId);
        harness.handlePermanentChosen(player1, zombiesId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Prince of Thralls"));

        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Prince of Thralls"));
    }
}
