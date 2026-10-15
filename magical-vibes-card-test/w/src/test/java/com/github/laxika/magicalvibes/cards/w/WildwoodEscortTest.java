package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildwoodEscort.class, GrizzlyBears.class, InvasionOfInnistrad.class, CruelEdict.class,
        LightningBolt.class, DelugeOfTheDead.class, Humble.class})
class WildwoodEscortTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted creature card from the graveyard to hand")
    void etbReturnsCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns a targeted battle card from the graveyard to hand")
    void etbReturnsBattleToHand() {
        InvasionOfInnistrad battle = new InvasionOfInnistrad();
        harness.setGraveyard(player1, List.of(battle));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(battle.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Invasion of Innistrad");
    }

    @Test
    @DisplayName("ETB does not target a noncreature, nonbattle card")
    void etbRejectsNonCreatureNonBattleCard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("When Wildwood Escort would die, it is exiled instead")
    void exiledInsteadOfDying() {
        harness.addToBattlefield(player1, new WildwoodEscort());

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Wildwood Escort");
        harness.assertNotInGraveyard(player1, "Wildwood Escort");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wildwood Escort"));
    }

    @Test
    @DisplayName("ETB cannot target a creature in an opponent's graveyard")
    void etbDoesNotReturnOpponentsCreature() {
        harness.setGraveyard(player2, List.of(new WildwoodEscort()));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wildwood Escort");
        harness.assertInGraveyard(player2, "Wildwood Escort");
        harness.assertNotInHand(player1, "Wildwood Escort");
    }

    @Test
    @DisplayName("ETB requires exactly one legal target and returns only that card")
    void etbRequiresExactlyOneTarget() {
        WildwoodEscort creature = new WildwoodEscort();
        InvasionOfInnistrad battle = new InvasionOfInnistrad();
        harness.setGraveyard(player1, List.of(creature, battle));

        castAndResolve();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), battle.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wildwood Escort");
        harness.assertNotInGraveyard(player1, "Wildwood Escort");
        harness.assertInGraveyard(player1, "Invasion of Innistrad");
        harness.assertNotInHand(player1, "Invasion of Innistrad");
    }

    @Test
    @DisplayName("ETB rejects an opponent's card even when your graveyard has a legal target")
    void etbRejectsOpponentsTarget() {
        WildwoodEscort ownCreature = new WildwoodEscort();
        WildwoodEscort opponentsCreature = new WildwoodEscort();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        castAndResolve();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(opponentsCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsCreature);
    }

    @Test
    @DisplayName("ETB does not choose a replacement target when the chosen card is exiled")
    void etbDoesNotRetargetAfterTargetIsExiled() {
        harness.addToBattlefieldAndReturn(player2, new DelugeOfTheDead()).setTransformed(true);
        WildwoodEscort creature = new WildwoodEscort();
        InvasionOfInnistrad battle = new InvasionOfInnistrad();
        harness.setGraveyard(player1, List.of(creature, battle));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.assertNotInHand(player1, "Wildwood Escort");
        harness.assertInGraveyard(player1, "Invasion of Innistrad");
        harness.assertNotInHand(player1, "Invasion of Innistrad");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still returns its target after lethal damage exiles Wildwood Escort")
    void etbResolvesAfterSourceIsExiled() {
        InvasionOfInnistrad battle = new InvasionOfInnistrad();
        harness.setGraveyard(player1, List.of(battle));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(battle.getId()));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Wildwood Escort"));

        harness.assertNotOnBattlefield(player1, "Wildwood Escort");
        harness.assertNotInGraveyard(player1, "Wildwood Escort");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wildwood Escort"));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Invasion of Innistrad");
        harness.assertNotInGraveyard(player1, "Invasion of Innistrad");
    }

    @Test
    @DisplayName("Wildwood Escort goes to the graveyard when it dies after losing all abilities")
    void diesNormallyAfterLosingAbilities() {
        harness.addToBattlefield(player1, new WildwoodEscort());
        harness.setHand(player2, List.of(new Humble(), new LightningBolt()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Wildwood Escort"));
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Wildwood Escort"));

        harness.assertNotOnBattlefield(player1, "Wildwood Escort");
        harness.assertInGraveyard(player1, "Wildwood Escort");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Wildwood Escort"));
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WildwoodEscort(), "{4}{G}");
        harness.passBothPriorities();
    }
}
