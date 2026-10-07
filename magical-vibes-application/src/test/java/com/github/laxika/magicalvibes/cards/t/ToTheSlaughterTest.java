package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.j.JaceUnravelerOfSecrets;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToTheSlaughter.class, DevilthornFox.class, JaceUnravelerOfSecrets.class,
        Forest.class, DualShot.class, MagnifyingGlass.class})
class ToTheSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, the target player chooses a creature or planeswalker")
    void withoutDeliriumTargetPlayerChoosesOnePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertOnBattlefield(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("With delirium, sacrifices a creature and a planeswalker")
    void withDeliriumSacrificesBothTypes() {
        setDelirium();
        harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With delirium, a two-permanent choice must include both types")
    void withDeliriumRequiresBothTypesWhenPossible() {
        setDelirium();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player2, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature and a planeswalker");

        harness.handleMultiplePermanentsChosen(player2, List.of(firstCreature.getId(), planeswalker.getId()));

        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondCreature);
    }

    @Test
    @DisplayName("To the Slaughter can target only a player")
    void targetMustBePlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deliriumSacrificesOnlyOneCreatureWhenNoPlaneswalkerExists() {
        setDelirium();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        castToTheSlaughter(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        harness.assertInGraveyard(player2, "Devilthorn Fox");
    }

    @Test
    void deliriumSacrificesPlaneswalkerWhenNoCreatureExists() {
        setDelirium();
        addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyBattlefieldDoesNotRequireSacrificeChoice() {
        setDelirium();
        harness.addToBattlefield(player2, new MagnifyingGlass());

        castToTheSlaughter(player2.getId());

        harness.assertOnBattlefield(player2, "Magnifying Glass");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void spellOnStackDoesNotSupplyFourthGraveyardType() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new MagnifyingGlass()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
    }

    @Test
    void deliriumIsCheckedAtResolution() {
        harness.addToBattlefield(player2, new DevilthornFox());
        addReadyJace(player2);
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, player2.getId());
        setDelirium();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
    }

    @Test
    @CardUsed({AlexiosDeimosOfKosmos.class})
    void deliriumCannotSacrificeCreatureThatCannotBeSacrificed() {
        setDelirium();
        harness.addToBattlefield(player2, new AlexiosDeimosOfKosmos());
        addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        harness.assertOnBattlefield(player2, "Alexios, Deimos of Kosmos");
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withoutDeliriumPlayerCanChooseCreatureInsteadOfPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        harness.assertInGraveyard(player2, "Devilthorn Fox");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    void targetPlayersGraveyardDoesNotEnableCastersDelirium() {
        harness.setGraveyard(player2, List.of(
                new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertInGraveyard(player2, "Jace, Unraveler of Secrets");
    }

    @Test
    void casterCanTargetThemselves() {
        setDelirium();
        harness.addToBattlefield(player1, new DevilthornFox());
        addReadyJace(player1);

        castToTheSlaughter(player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Jace, Unraveler of Secrets");
    }

    private void castToTheSlaughter(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = harness.addToBattlefieldAndReturn(player, new JaceUnravelerOfSecrets());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        return jace;
    }
}
