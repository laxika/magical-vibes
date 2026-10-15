package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.c.CrownOfConvergence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reroute.class, ViashinoFangtail.class, Forest.class, Char.class, CrownOfConvergence.class})
class RerouteTest extends BaseCardTest {

    @Test
    @DisplayName("Changes the target of a single-target activated ability and draws a card")
    void reroutesActivatedAbilityAndDraws() {
        ViashinoFangtail fangtail = new ViashinoFangtail();
        addCreatureReady(player2, fangtail);

        harness.setHand(player1, List.of(new Reroute()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, fangtail.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Can redirect an ability from a player to a creature, including its source")
    void redirectsAbilityToItsSource() {
        ViashinoFangtail fangtail = new ViashinoFangtail();
        Permanent source = addCreatureReady(player2, fangtail);
        harness.setHand(player1, List.of(new Reroute()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, fangtail.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(source.getId())
                .doesNotContain(player1.getId());
        harness.handlePermanentChosen(player1, source.getId());
        resolveAllTriggers();

        assertThat(source.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Reroute");
    }

    @Test
    @DisplayName("Can reroute an activated ability after its source has been destroyed")
    void reroutesAbilityAfterSourceLeavesBattlefield() {
        ViashinoFangtail fangtail = new ViashinoFangtail();
        Permanent source = addCreatureReady(player2, fangtail);
        harness.setHand(player1, List.of(new Char(), new Reroute()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertInGraveyard(player2, "Viashino Fangtail");

        harness.castAndResolveInstant(player1, 0, fangtail.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(source.getId(), player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, player1LifeBefore - 2);
        harness.assertLife(player2, player2LifeBefore - 1);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Reroute");
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        harness.setHand(player1, List.of(new Reroute()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Char charSpell = new Char();
        harness.setHand(player2, List.of(charSpell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, charSpell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activated ability");
    }

    @Test
    @DisplayName("Cannot target an activated ability without a target")
    void cannotTargetUntargetedActivatedAbility() {
        CrownOfConvergence crown = new CrownOfConvergence();
        harness.addToBattlefield(player2, crown);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Reroute()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crown.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single-target activated ability");
    }
}
