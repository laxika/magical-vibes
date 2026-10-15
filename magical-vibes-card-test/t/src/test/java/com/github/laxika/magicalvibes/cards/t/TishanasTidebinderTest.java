package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.r.RestlessAnchorage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TishanasTidebinder.class, IcyManipulator.class, GrizzlyBears.class,
        MerfolkTrickster.class, Shock.class, RestlessAnchorage.class})
class TishanasTidebinderTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and removes all abilities from its artifact source")
    void countersActivatedAbilityAndRemovesSourceAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);
        var activatedAbilitySourceId = gd.stack.getFirst().getCard().getId();

        harness.castFromHand(player1, new TishanasTidebinder(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, activatedAbilitySourceId);
        harness.passBothPriorities();

        Permanent bearsPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(bearsPermanent.isTapped()).isFalse();
        icyManipulator.untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tishana's Tidebinder"));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(bearsPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters a triggered ability and removes the triggered source's abilities")
    void countersTriggeredAbilityAndRemovesSourceAbilities() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        MerfolkTrickster trickster = new MerfolkTrickster();
        harness.setHand(player2, List.of(trickster));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        var triggeredAbilitySourceId = gd.stack.getFirst().getCard().getId();

        harness.castFromHand(player1, new TishanasTidebinder(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, triggeredAbilitySourceId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd,
                findPermanent(player2, "Merfolk Trickster"), Keyword.FLASH)).isFalse();
    }

    @Test
    @DisplayName("Can resolve with no target")
    void canResolveWithNoTarget() {
        harness.castFromHand(player1, new TishanasTidebinder(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a land's animation ability without removing its abilities")
    void countersLandAbilityWithoutRemovingAbilities() {
        Permanent anchorage = harness.addToBattlefieldAndReturn(player2, new RestlessAnchorage());
        anchorage.untap();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        var abilityId = gd.stack.getFirst().getTargetableId();

        harness.castFromHand(player1, new TishanasTidebinder(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, anchorage)).isFalse();
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(anchorage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still counters when Tidebinder leaves before its trigger resolves, without removing abilities")
    void leavingBeforeResolutionDoesNotRemoveAbilities() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent icy = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, bears.getId());
        var abilityId = gd.stack.getFirst().getTargetableId();

        harness.castFromHand(player1, new TishanasTidebinder(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, abilityId);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tishana's Tidebinder"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Tishana's Tidebinder");
        icy.untap();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.isTapped()).isTrue();
    }
}
