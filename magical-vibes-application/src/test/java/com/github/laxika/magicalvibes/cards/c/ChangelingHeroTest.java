package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChangelingHero.class, FieldMarshal.class, GrizzlyBears.class, Unsummon.class, Island.class})
class ChangelingHeroTest extends BaseCardTest {

    private void castChangelingHero() {
        harness.setHand(player1, List.of(new ChangelingHero()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other creatures")
    void autoSacrificesWithNoOtherCreatures() {
        castChangelingHero();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Changeling Hero");
        harness.assertInGraveyard(player1, "Changeling Hero");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not accept a noncreature permanent for champion")
    void doesNotChampionNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Island());
        castChangelingHero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Changeling Hero");
        harness.assertInGraveyard(player1, "Changeling Hero");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB with another creature prompts champion choice")
    void etbWithAnotherCreaturePromptsChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castChangelingHero();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Changeling Hero");
    }

    @Test
    @DisplayName("Championing a creature exiles it and keeps Changeling Hero")
    void championingExilesCreatureAndKeepsHero() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castChangelingHero();
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Changeling Hero");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Championed creature returns when Changeling Hero leaves the battlefield")
    void championedCreatureReturnsWhenHeroLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castChangelingHero();
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID heroId = harness.getPermanentId(player1, "Changeling Hero");
        harness.castAndResolveInstant(player1, 0, heroId);

        harness.assertNotOnBattlefield(player1, "Changeling Hero");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if Changeling Hero leaves before its champion ability resolves")
    void championAbilityDoesNothingIfHeroLeavesBeforeResolution() {
        castChangelingHero();

        UUID heroId = harness.getPermanentId(player1, "Changeling Hero");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, heroId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Changeling Hero");
        harness.assertInHand(player1, "Changeling Hero");
        harness.assertNotInGraveyard(player1, "Changeling Hero");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Entry ability can still exile another creature after Hero leaves")
    void canChampionAfterHeroLeavesBeforeEntryAbilityResolves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castChangelingHero();

        UUID heroId = harness.getPermanentId(player1, "Changeling Hero");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, heroId);
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, bearsId);
        resolveAllTriggers();

        harness.assertInHand(player1, "Changeling Hero");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Champion rejects Hero itself and creatures controlled by the opponent")
    void championRejectsSelfAndOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FieldMarshal());
        castChangelingHero();
        harness.passBothPriorities();

        UUID heroId = harness.getPermanentId(player1, "Changeling Hero");
        UUID marshalId = harness.getPermanentId(player2, "Field Marshal");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, heroId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, marshalId))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertOnBattlefield(player1, "Changeling Hero");
        harness.assertOnBattlefield(player2, "Field Marshal");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Lifelink gains life equal to combat damage dealt")
    void lifelinkGainsLifeInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ChangelingHero());
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Changeling gets boost from Soldier lord")
    void changelingGetsSoldierLordBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new ChangelingHero());

        Permanent hero = findPermanent(player1, "Changeling Hero");

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.FIRST_STRIKE)).isTrue();
    }
}
