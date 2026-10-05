package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LilianaDeathsMajesty;
import com.github.laxika.magicalvibes.cards.s.ScarabFeast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeverReturn.class, GrizzlyBears.class, Island.class, Shock.class,
        Colossapede.class, LilianaDeathsMajesty.class, ScarabFeast.class})
class NeverReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Never destroys target creature")
    void neverDestroysCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NeverReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Never");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Never cannot target a land")
    void neverCannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new NeverReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return exiles target graveyard card, creates Zombie, then exiles itself")
    void returnExilesCardCreatesZombieAndExiles() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(new NeverReturn()));
        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, shock.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Never") || c.getName().equals("Return"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Never"));
    }

    @Test
    @DisplayName("Return can exile a noncreature card from own graveyard")
    void returnExilesNoncreatureFromOwnGraveyard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new NeverReturn(), shock)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, shock.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        harness.assertOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Return requires sorcery timing")
    void returnRequiresSorceryTiming() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(new NeverReturn()));
        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Never destroys a planeswalker")
    void neverDestroysPlaneswalker() {
        Permanent liliana = harness.enterBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        harness.setHand(player1, List.of(new NeverReturn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, liliana.getId());

        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player1, "Never");
    }

    @Test
    @DisplayName("Return exiles a creature and creates exactly one 2/2 black Zombie")
    void returnExilesCreatureAndCreatesZombie() {
        Card creature = new Colossapede();
        Card spell = new NeverReturn();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertNotInGraveyard(player2, "Colossapede");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Return cannot be cast without a graveyard target")
    void returnRequiresGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new NeverReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Return creates no Zombie when its only target is exiled in response")
    void returnFizzlesWhenTargetLeavesGraveyard() {
        Card creature = new Colossapede();
        Card spell = new NeverReturn();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player2, List.of(new ScarabFeast()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castFlashback(player1, 0, creature.getId());
        harness.castInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Never");
    }
}
