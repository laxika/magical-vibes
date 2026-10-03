package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.cards.s.ScatterArc;
import com.github.laxika.magicalvibes.cards.s.Shambleshark;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({DiluvianPrimordial.class, GrizzlyBears.class, Shock.class, Mugging.class,
        ScatterArc.class, Shambleshark.class})
class DiluvianPrimordialTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a targeted instant from an opponent's graveyard without paying its mana cost")
    void castsTargetedInstantFromOpponentGraveyard() {
        Shock shock = new Shock();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Shock");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Allows at most one target from each opponent's graveyard")
    void allowsAtMostOneTargetPerOpponentGraveyard() {
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setGraveyard(player2, List.of(firstShock, secondShock));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstShock.getId(), secondShock.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casts a sorcery during the triggered ability without paying its mana cost")
    void castsSorceryFromOpponentGraveyard() {
        Mugging mugging = new Mugging();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Shambleshark());
        harness.setGraveyard(player2, List.of(mugging));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(mugging.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shambleshark");
        harness.assertNotInGraveyard(player2, "Mugging");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(mugging.getId()));
    }

    @Test
    @DisplayName("Declining to cast leaves the targeted card in its owner's graveyard")
    void decliningLeavesCardInGraveyard() {
        Mugging mugging = new Mugging();
        harness.setGraveyard(player2, List.of(mugging));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(mugging.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Mugging");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(mugging.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero targets leaves eligible graveyard cards untouched")
    void allowsZeroTargets() {
        Mugging mugging = new Mugging();
        harness.setGraveyard(player2, List.of(mugging));

        harness.castFromHand(player1, new DiluvianPrimordial(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Diluvian Primordial");
        harness.assertInGraveyard(player2, "Mugging");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A card that cannot be cast because it has no legal targets stays in the graveyard")
    void uncastableCardIsNotExiled() {
        ScatterArc scatterArc = new ScatterArc();
        harness.setGraveyard(player2, List.of(scatterArc));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(scatterArc.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Scatter Arc");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(scatterArc.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an instant or sorcery in the controller's graveyard")
    void rejectsOwnGraveyard() {
        Mugging mugging = new Mugging();
        harness.setGraveyard(player1, List.of(mugging));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(mugging.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void rejectsCreatureCard() {
        Shambleshark creature = new Shambleshark();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution cannot be cast")
    void targetRemovedBeforeResolution() {
        Mugging mugging = new Mugging();
        harness.setGraveyard(player2, List.of(mugging));
        harness.setHand(player1, List.of(new DiluvianPrimordial()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(mugging.getId()));
        harness.passBothPriorities();
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(mugging));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(mugging.getId()));
    }
}
