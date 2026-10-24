package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.b.BenthicBehemoth;
import com.github.laxika.magicalvibes.cards.s.SkyshroudTroll;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Extinction.class, AvianChangeling.class, BayouDragonfly.class, BenthicBehemoth.class,
        SkyshroudTroll.class})
class ExtinctionTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.castFromHand(player, new Extinction(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys every creature of the chosen type, on any battlefield")
    void destroysAllCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BenthicBehemoth());

        payAndCast(player1);
        harness.handleListChoice(player1, "SERPENT");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures of other types survive")
    void otherTypesSurvive() {
        harness.addToBattlefield(player1, new BayouDragonfly());
        harness.addToBattlefield(player2, new BenthicBehemoth());

        payAndCast(player1);
        harness.handleListChoice(player1, "SERPENT");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Changeling is of every creature type and is destroyed")
    void changelingIsDestroyed() {
        harness.addToBattlefield(player2, new AvianChangeling());

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a type nobody controls destroys nothing")
    void chosenTypeNobodyControlsDestroysNothing() {
        harness.addToBattlefield(player1, new BenthicBehemoth());
        harness.addToBattlefield(player2, new BayouDragonfly());

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The creature type is chosen during resolution, not while casting")
    void choosesTypeDuringResolution() {
        harness.addToBattlefield(player2, new BenthicBehemoth());

        harness.castFromHand(player1, new Extinction(), "{4}{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.handleListChoice(player1, "SERPENT");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Benthic Behemoth");
        harness.assertInGraveyard(player1, "Extinction");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves on an empty battlefield and still asks for a creature type")
    void resolvesOnEmptyBattlefield() {
        payAndCast(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "SERPENT");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Extinction");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield saves a matching creature while unshielded creatures die")
    void matchingCreatureCanRegenerate() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SkyshroudTroll());
        harness.addToBattlefield(player2, new SkyshroudTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        payAndCast(player1);
        harness.handleListChoice(player1, "GIANT");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(troll);
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Skyshroud Troll");
        harness.assertInGraveyard(player2, "Skyshroud Troll");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Asks the death replacement after the type is chosen, before any creature is destroyed")
    void asksTheDeathReplacementBeforeAnyCreatureIsDestroyed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent otherZombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());

        payAndCast(player1);
        harness.handleListChoice(player1, "ZOMBIE");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gravebane, otherZombie);

        harness.handleListChoice(player2, "Put it into its owner's library");
        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        List<UUID> library = gd.playerDecks.get(player2.getId()).stream().map(Card::getId).toList();
        List<Permanent> zombies = List.of(gravebane, otherZombie);
        assertThat(zombies).filteredOn(zombie -> library.contains(zombie.getCard().getId())).hasSize(1);
        assertThat(zombies).filteredOn(zombie -> gd.findExiledCard(zombie.getCard().getId()) != null).hasSize(1);
        harness.assertNotInGraveyard(player2, "Gravebane Zombie");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        harness.assertInGraveyard(player1, "Extinction");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
