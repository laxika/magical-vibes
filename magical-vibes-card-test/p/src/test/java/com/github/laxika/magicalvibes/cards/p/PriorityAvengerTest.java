package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriorityAvenger.class, Shock.class, GrizzlyBears.class, MoggFanatic.class, IsochronScepter.class,
        Shatter.class, AetherSpellbomb.class})
class PriorityAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Players cannot cast instants while the stack is empty")
    void blocksInstantsWithEmptyStack() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An instant can be cast while a spell is on the stack")
    void allowsInstantWithSpellOnStack() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("An instant can be cast while an ability is on the stack")
    void allowsInstantWithAbilityOnStack() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Priority Avenger's restriction applies to every player")
    void restrictionIsSymmetric() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction returns after the last stack entry resolves")
    void blocksInstantsAgainAfterStackEmpties() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Instants can be cast on an empty stack after Priority Avenger dies")
    void restrictionEndsWhenAvengerLeavesBattlefield() {
        harness.addToBattlefield(player1, new PriorityAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var avengerId = harness.getPermanentId(player1, "Priority Avenger");

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, avengerId);
        harness.castAndResolveInstant(player1, 0, avengerId);
        harness.assertInGraveyard(player1, "Priority Avenger");
        harness.assertNotOnBattlefield(player1, "Priority Avenger");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An instant can be cast while the ability that lets it be cast resolves, as that ability is on the stack")
    void allowsInstantCastDuringAnAbilitysResolution() {
        harness.addToBattlefield(player2, new PriorityAvenger());
        IsochronScepter scepterCard = new IsochronScepter();
        Shatter shatterCard = new Shatter();
        gd.setImprintedCard(scepterCard, shatterCard);
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, scepterCard);
        gd.exiledCards.add(new ExiledCardEntry(shatterCard, player1.getId(), scepterCard.getId()));
        scepter.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shatter") && entry.isCopy());
    }
}
