package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.f.FleetingDistraction;
import com.github.laxika.magicalvibes.cards.s.SkitteringInvasion;
import com.github.laxika.magicalvibes.cards.s.SkywatcherAdept;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CastThroughTime.class, AllIsDust.class, FleetingDistraction.class,
        SkitteringInvasion.class, SkywatcherAdept.class})
class CastThroughTimeTest extends BaseCardTest {

    @Test
    @CardUsed(DarkRitual.class)
    void givesControlledInstantRebound() {
        harness.addToBattlefield(player1, new CastThroughTime());

        DarkRitual spell = new DarkRitual();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    void sorceryReboundsOnlyOnceAndStillCastsAfterEnchantmentLeaves() {
        harness.addToBattlefield(player1, new CastThroughTime());
        SkitteringInvasion spell = new SkitteringInvasion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Eldrazi Spawn")).isEqualTo(5);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof CastThroughTime);

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Eldrazi Spawn")).isEqualTo(10);
        harness.assertInGraveyard(player1, "Skittering Invasion");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOpportunity() {
        harness.addToBattlefield(player1, new CastThroughTime());
        SkitteringInvasion spell = new SkitteringInvasion();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(countPermanents(player1, "Eldrazi Spawn")).isEqualTo(5);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotGrantReboundToOpponentsInstant() {
        harness.addToBattlefield(player1, new CastThroughTime());
        var creature = harness.addToBattlefieldAndReturn(player1, new SkywatcherAdept());
        FleetingDistraction spell = new FleetingDistraction();
        harness.setHand(player2, List.of(spell));
        harness.setLibrary(player2, List.of(new SkywatcherAdept()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player2, "Fleeting Distraction");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void spellThatRemovesCastThroughTimeDuringResolutionDoesNotRebound() {
        harness.addToBattlefield(player1, new CastThroughTime());
        AllIsDust spell = new AllIsDust();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Cast Through Time");
        harness.assertInGraveyard(player1, "All Is Dust");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void spellWithIllegalTargetDoesNotRebound() {
        harness.addToBattlefield(player1, new CastThroughTime());
        var creature = harness.addToBattlefieldAndReturn(player1, new SkywatcherAdept());
        FleetingDistraction spell = new FleetingDistraction();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new SkywatcherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fleeting Distraction");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
