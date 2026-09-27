package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeodeGolem.class, GrizzlyBears.class})
class GeodeGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers the controller's commander for free")
    void castsCommanderFromCommandZoneWithoutPayingMana() {
        addAttackingGeodeGolem();
        Card commander = putCommanderInCommandZone();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId())
                && entry.getSourceZone() == Zone.COMMAND);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(gd.commanderTaxByCardId).containsEntry(commander.getId(), 2);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the commander in the command zone")
    void decliningLeavesCommanderInCommandZone() {
        addAttackingGeodeGolem();
        Card commander = putCommanderInCommandZone();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerCommandZones.get(player1.getId())).extracting(Card::getId)
                .containsExactly(commander.getId());
    }

    @Test
    @DisplayName("Does not offer a card when the controller has no commander")
    void doesNotOfferWithoutCommander() {
        addAttackingGeodeGolem();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingGeodeGolem() {
        Permanent geode = addCreatureReady(player1, new GeodeGolem());
        geode.setAttacking(true);
        return geode;
    }

    private Card putCommanderInCommandZone() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
