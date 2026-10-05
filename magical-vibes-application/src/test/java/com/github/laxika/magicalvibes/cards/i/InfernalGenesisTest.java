package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.MunghaWurm;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalGenesis.class, MunghaWurm.class, WintermoonMesa.class,
        LeylineOfTheVoid.class, LeylineOfSanctity.class})
class InfernalGenesisTest extends BaseCardTest {

    @Test
    @DisplayName("The active player mills a card and creates Minions equal to its mana value")
    void activePlayerMillsAndCreatesTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.setLibrary(player2, List.of(new MunghaWurm()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mungha Wurm");
        assertThat(countPermanents(player2, "Minion")).isEqualTo(4);
        assertThat(countPermanents(player1, "Minion")).isZero();

        Permanent minion = findPermanent(player2, "Minion");
        assertThat(minion.getCard().isToken()).isTrue();
        assertThat(minion.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(minion.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(minion.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(minion.getCard().getSubtypes()).containsExactly(CardSubtype.MINION);
        assertThat(minion.getEffectivePower()).isEqualTo(1);
        assertThat(minion.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A mana value zero card creates no Minions but is still milled")
    void manaValueZeroCreatesNoTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.setLibrary(player2, List.of(new WintermoonMesa()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Wintermoon Mesa");
        assertThat(countPermanents(player2, "Minion")).isZero();
    }

    @Test
    @DisplayName("An empty library produces no Minions")
    void emptyLibraryCreatesNoTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player2, "Minion")).isZero();
    }

    @Test
    @DisplayName("The controller also mills and creates Minions on their own upkeep")
    void controllerUpkeepCreatesTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.setLibrary(player1, List.of(new MunghaWurm(), new WintermoonMesa()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mungha Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Wintermoon Mesa");
        assertThat(countPermanents(player1, "Minion")).isEqualTo(4);
        assertThat(countPermanents(player2, "Minion")).isZero();
    }

    @Test
    @DisplayName("Two copies mill successive cards and create tokens for each card's mana value")
    void multipleCopiesResolveIndependently() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.addToBattlefield(player2, new InfernalGenesis());
        harness.setLibrary(player2, List.of(new MunghaWurm(), new InfernalGenesis(), new WintermoonMesa()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mungha Wurm", "Infernal Genesis");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Wintermoon Mesa");
        assertThat(countPermanents(player2, "Minion")).isEqualTo(10);
        assertThat(countPermanents(player1, "Minion")).isZero();
    }

    @Test
    @DisplayName("A milled card diverted to face-up exile still determines the number of Minions")
    void exiledMilledCardStillCreatesTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.setLibrary(player2, List.of(new MunghaWurm(), new WintermoonMesa()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactly("Mungha Wurm");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Wintermoon Mesa");
        assertThat(countPermanents(player2, "Minion")).isEqualTo(4);
        assertThat(countPermanents(player1, "Minion")).isZero();
    }

    @Test
    @DisplayName("The upkeep ability does not target and affects an opponent with hexproof")
    void opponentHexproofDoesNotPreventMillOrTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLibrary(player2, List.of(new MunghaWurm(), new WintermoonMesa()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mungha Wurm");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Wintermoon Mesa");
        assertThat(countPermanents(player2, "Minion")).isEqualTo(4);
        assertThat(countPermanents(player1, "Minion")).isZero();
    }
}
