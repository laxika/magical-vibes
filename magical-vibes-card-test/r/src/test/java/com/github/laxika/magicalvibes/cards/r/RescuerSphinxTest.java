package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CommandTheDreadhorde;
import com.github.laxika.magicalvibes.cards.g.GuildGlobe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KronchWrangler;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RescuerSphinx.class, GrizzlyBears.class, Island.class, GuildGlobe.class,
        ParadiseDruid.class, CommandTheDreadhorde.class, KronchWrangler.class})
class RescuerSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return another nonland permanent and put a counter on Rescuer Sphinx")
    void returnsPermanentAndEntersWithCounter() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB choice leaves permanents unchanged")
    void decliningLeavesPermanentsUnchanged() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lands are not legal choices")
    void landCannotBeReturned() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Island");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    @Test
    void returnsNoncreaturePermanent() {
        UUID globeId = harness.addToBattlefieldAndReturn(player1, new GuildGlobe()).getId();
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, globeId);

        harness.assertInHand(player1, "Guild Globe");
        harness.assertNotOnBattlefield(player1, "Guild Globe");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotReturnOpponentPermanentOrItself() {
        harness.addToBattlefield(player2, new ParadiseDruid());
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Paradise Druid");
        harness.assertOnBattlefield(player1, "Rescuer Sphinx");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotReturnPermanentEnteringSimultaneously() {
        RescuerSphinx sphinx = new RescuerSphinx();
        ParadiseDruid druid = new ParadiseDruid();
        harness.setGraveyard(player1, List.of(druid, sphinx));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(druid.getId(), sphinx.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Paradise Druid");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void simultaneousEntryPutsCounterOnSphinxRatherThanLastCreature() {
        UUID globeId = harness.addToBattlefieldAndReturn(player1, new GuildGlobe()).getId();
        RescuerSphinx sphinx = new RescuerSphinx();
        ParadiseDruid druid = new ParadiseDruid();
        harness.setGraveyard(player1, List.of(sphinx, druid));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(sphinx.getId(), druid.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, globeId);

        harness.assertInHand(player1, "Guild Globe");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Paradise Druid").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returnsControlledPermanentToItsOwner() {
        GuildGlobe globe = new GuildGlobe();
        globe.setOwnerId(player2.getId());
        UUID globeId = harness.addToBattlefieldAndReturn(player1, globe).getId();
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, globeId);

        harness.assertInHand(player2, "Guild Globe");
        harness.assertNotInHand(player1, "Guild Globe");
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringCounterIsIncludedInPowerForEntryTriggers() {
        harness.addToBattlefield(player1, new KronchWrangler());
        UUID globeId = harness.addToBattlefieldAndReturn(player1, new GuildGlobe()).getId();
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, globeId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Kronch Wrangler").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returnedPermanentDoesNotSeeSphinxEnter() {
        UUID wranglerId = harness.addToBattlefieldAndReturn(player1, new KronchWrangler()).getId();
        harness.castFromHand(player1, new RescuerSphinx(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, wranglerId);

        harness.assertInHand(player1, "Kronch Wrangler");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Rescuer Sphinx").getCounterCount(
                CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
