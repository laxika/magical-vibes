package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BurningOfXinyeTest extends BaseCardTest {

    private static Card bigCreature() {
        Card card = new Card();
        card.setName("Big Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{4}{G}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(6);
        card.setToughness(6);
        return card;
    }

    private void castBurning() {
        harness.setHand(player1, List.of(new BurningOfXinye()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both players with four or fewer lands lose all their lands")
    void bothPlayersLoseAllLandsWhenFourOrFewer() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        castBurning();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Controller with more than four lands chooses which four to destroy")
    void controllerChoosesWhichFourToDestroy() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        castBurning();

        // Controller (player1) is prompted to choose 4 of their 6 lands.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        List<UUID> chosen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(4).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, chosen);

        // Player1 keeps 2 lands; the opponent's two lands are all destroyed automatically.
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Controller destroys their lands before the opponent chooses theirs")
    void bothPlayersChooseSequentially() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        castBurning();

        // Controller chooses first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        List<UUID> p1Chosen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(4).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, p1Chosen);

        // Controller's four lands are destroyed immediately (not deferred), leaving 2.
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(2);

        // Now the opponent is prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        List<UUID> p2Chosen = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(4).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, p2Chosen);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 4 damage to each creature, killing toughness 4 or less but not larger")
    void dealsFourDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, bigCreature());      // 6/6

        castBurning();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player2, "Big Creature");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castBurning();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only lands are destroyed, non-land permanents are merely damaged")
    void onlyLandsDestroyed() {
        harness.addToBattlefield(player1, bigCreature()); // 6/6 survives
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        castBurning();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.assertOnBattlefield(player1, "Big Creature");
    }
}
