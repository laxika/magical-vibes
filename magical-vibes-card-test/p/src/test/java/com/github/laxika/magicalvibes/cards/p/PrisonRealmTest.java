package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.d.Despark;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrisonRealm.class, GideonBlackblade.class, PrimordialWurm.class, ReturnToNature.class, Forest.class, Despark.class})
class PrisonRealmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent's creature and scries 1")
    void exilesCreatureAndScries() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID bearsId = harness.getPermanentId(player2, "Primordial Wurm");

        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Primordial Wurm"));
    }

    @Test
    @DisplayName("ETB can exile an opponent's planeswalker")
    void exilesPlaneswalker() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        UUID gideonId = harness.getPermanentId(player2, "Gideon Blackblade");

        castAndResolve(gideonId);

        harness.assertNotOnBattlefield(player2, "Gideon Blackblade");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Gideon Blackblade"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Prison Realm leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID bearsId = harness.getPermanentId(player2, "Primordial Wurm");

        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID prisonRealmId = harness.getPermanentId(player1, "Prison Realm");
        harness.castModalInstant(player2, 0, 1, List.of(prisonRealmId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Primordial Wurm"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new PrimordialWurm());
        UUID bearsId = harness.getPermanentId(player1, "Primordial Wurm");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Scry still triggers when no opponent has a creature or planeswalker")
    void scriesWithoutAnExileTarget() {
        setUpCast();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prison Realm");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishScry();
    }

    @Test
    @DisplayName("Exile and scry are separate triggered abilities on the stack")
    void enterAbilitiesUseSeparateStackEntries() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        finishScry();
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
    }

    @Test
    @DisplayName("Scry still resolves when the exile target leaves in response")
    void scriesWhenExileTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Despark()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishScry();
    }

    @Test
    @DisplayName("Leaving before the exile ability resolves prevents exile but not scry")
    void sourceLeavesBeforeExileResolves() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Prison Realm");
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castModalInstant(player2, 0, 1, List.of(sourceId));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Prison Realm");
        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishScry();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom of the library")
    void scryCanPutCardOnBottom() {
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player2, "Primordial Wurm");
        setUpCast();
        Forest top = new Forest();
        PrimordialWurm next = new PrimordialWurm();
        harness.setLibrary(player1, List.of(top, next));
        harness.castEnchantment(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        resolveAllTriggers();
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishScry();
        resolveAllTriggers();
    }

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrisonRealm()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void finishScry() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
