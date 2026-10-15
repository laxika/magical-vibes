package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlvenwaldMysteries.class, GrizzlyBears.class, WrathOfGod.class, PlanarCleansing.class})
class UlvenwaldMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creature death investigates — creates a Clue token")
    void nontokenDeathInvestigates() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Does not investigate when a token creature dies")
    void doesNotInvestigateOnTokenDeath() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());

        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(tokenCard));

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Sacrificing a Clue creates a 1/1 white Human Soldier token")
    void clueSacrificeCreatesHumanSoldier() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        addClueToken(player1);

        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Clue");

        List<Permanent> soldiers = findPermanents(player1, "Human Soldier");
        assertThat(soldiers).hasSize(1);
        Permanent soldier = soldiers.getFirst();
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not create a Human Soldier")
    void nonClueSacrificeDoesNotCreateSoldier() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());

        Card creature = new Card();
        creature.setName("Goblin Token");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of(CardSubtype.GOBLIN));
        creature.setPower(1);
        creature.setToughness(1);
        creature.setToken(true);
        Permanent goblin = new Permanent(creature);
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerGraveyards.get(player1.getId()).add(goblin.getCard());
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), goblin.getCard()));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    @DisplayName("Each allied nontoken death investigates, but opposing deaths do not")
    void simultaneousDeathsInvestigateOncePerAlliedCreature() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Clue");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An investigated Clue creates a Soldier before its draw ability resolves")
    void investigatedClueSacrificeCreatesSoldierAndDraws() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.activateAbility(player1, clueIndex, null, null);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not create a Soldier")
    void opponentClueSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        addClueToken(player2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Human Soldier");
        harness.assertNotOnBattlefield(player2, "Human Soldier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Investigates for each allied nontoken creature destroyed together with Mysteries")
    void simultaneousDestructionOfMysteriesStillInvestigates() {
        harness.addToBattlefield(player1, new UlvenwaldMysteries());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Ulvenwald Mysteries");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    private void addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setColor(null);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
    }
}
