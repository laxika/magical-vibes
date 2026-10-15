package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BastionOfRemembrance;
import com.github.laxika.magicalvibes.cards.n.NyxbornMarauder;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AniktheaHandOfErebos.class, NyxbornMarauder.class, SongOfTheDryads.class, GrizzlyBears.class, BastionOfRemembrance.class})
class AniktheaHandOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other enchantment creatures you control menace")
    void givesEnchantmentCreaturesMenace() {
        Permanent anikthea = harness.addToBattlefieldAndReturn(player1, new AniktheaHandOfErebos());
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornMarauder());
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentEnchantmentCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornMarauder());

        assertThat(gqs.hasKeyword(gd, anikthea, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantmentCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ordinaryCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEnchantmentCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Enters and creates a black 3/3 Zombie copy of a non-Aura enchantment")
    void entersAndCreatesNonAuraEnchantmentCopy() {
        NyxbornMarauder marauder = new NyxbornMarauder();
        SongOfTheDryads aura = new SongOfTheDryads();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(marauder, aura, bears));
        harness.enterBattlefieldAndReturn(player1, new AniktheaHandOfErebos());

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(marauder.getId());

        harness.handleMultipleCardsChosen(player1, List.of(marauder.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Nyxborn Marauder");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getSubtypes()).containsAll(marauder.getSubtypes());
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Song of the Dryads");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking creates the same optional copy trigger")
    void attackingCreatesCopyTrigger() {
        addCreatureReady(player1, new AniktheaHandOfErebos());

        NyxbornMarauder marauder = new NyxbornMarauder();
        harness.setGraveyard(player1, List.of(marauder));
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(marauder.getId());
        harness.handleMultipleCardsChosen(player1, List.of(marauder.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Nyxborn Marauder"));
    }

    @Test
    @DisplayName("A copied noncreature enchantment is a Zombie creature and retains its enters ability")
    void noncreatureEnchantmentBecomesZombieCreature() {
        BastionOfRemembrance bastion = new BastionOfRemembrance();
        harness.setGraveyard(player1, List.of(bastion));
        harness.enterBattlefieldAndReturn(player1, new AniktheaHandOfErebos());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bastion.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Bastion of Remembrance");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Bastion of Remembrance");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bastion.getId()));
    }

    @Test
    @DisplayName("The enters trigger can choose zero targets even with eligible cards")
    void entersCanChooseZeroTargetsAndExcludesOpponentsGraveyard() {
        BastionOfRemembrance ownCard = new BastionOfRemembrance();
        BastionOfRemembrance opposingCard = new BastionOfRemembrance();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.enterBattlefieldAndReturn(player1, new AniktheaHandOfErebos());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bastion of Remembrance");
        harness.assertInGraveyard(player2, "Bastion of Remembrance");
        harness.assertNotOnBattlefield(player1, "Bastion of Remembrance");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger can choose zero targets")
    void attackingCanChooseZeroTargets() {
        addCreatureReady(player1, new AniktheaHandOfErebos());
        harness.setGraveyard(player1, List.of(new BastionOfRemembrance()));
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bastion of Remembrance");
        harness.assertNotOnBattlefield(player1, "Bastion of Remembrance");
    }

    @Test
    @DisplayName("No token is created when the selected card leaves the graveyard before resolution")
    void missingTargetCreatesNoToken() {
        BastionOfRemembrance bastion = new BastionOfRemembrance();
        harness.setGraveyard(player1, List.of(bastion));
        harness.enterBattlefieldAndReturn(player1, new AniktheaHandOfErebos());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bastion.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bastion of Remembrance");
        assertThat(gd.exiledCards).isEmpty();
    }
}
