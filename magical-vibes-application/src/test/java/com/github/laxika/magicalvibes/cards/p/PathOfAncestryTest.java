package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfAncestry.class, GrizzlyBears.class, ElvishMystic.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new PathOfAncestry());

        assertThat(findPermanent(player1, "Path of Ancestry").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a matching creature spell causes scry 1")
    void matchingCreatureSpellTriggersScry() {
        preparePathAndCommander();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger for a different creature type")
    void differentCreatureTypeDoesNotTrigger() {
        preparePathAndCommander();
        harness.setHand(player1, List.of(new ElvishMystic()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when the matching creature uses mana from another source")
    void manaFromAnotherSourceDoesNotTrigger() {
        prepareCommanderAndPath();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    private void preparePathAndCommander() {
        prepareCommanderAndPath();
        Permanent path = findPermanent(player1, "Path of Ancestry");
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
    }

    private void prepareCommanderAndPath() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));

        harness.addToBattlefield(player1, new PathOfAncestry());
    }
}
