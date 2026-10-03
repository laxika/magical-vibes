package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MinisterOfImpediments;
import com.github.laxika.magicalvibes.cards.o.OcularHalo;
import com.github.laxika.magicalvibes.cards.r.RakdosIckspitter;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptChampion.class, MinisterOfImpediments.class, OcularHalo.class,
        RakdosIckspitter.class, TransguildCourier.class})
class CryptChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one qualifying creature from each graveyard and survives when red mana was spent")
    void returnsQualifyingCreatureFromEachGraveyardAndSurvivesWithRedMana() {
        Card ownCreature = new RakdosIckspitter();
        Card opponentCreature = new RakdosIckspitter();
        Card tooExpensive = new TransguildCourier();
        harness.setGraveyard(player1, List.of(ownCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castCryptChampion(true);
        resolveAllTriggers();

        assertThat(findPermanent(player1, ownCreature.getName()).getCard()).isSameAs(ownCreature);
        assertThat(findPermanent(player2, opponentCreature.getName()).getCard()).isSameAs(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
        harness.assertOnBattlefield(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Returns qualifying creatures before sacrificing itself when red mana was not spent")
    void returnsQualifyingCreaturesThenSacrificesWithoutRedMana() {
        Card ownCreature = new RakdosIckspitter();
        Card opponentCreature = new RakdosIckspitter();
        Card tooExpensive = new TransguildCourier();
        harness.setGraveyard(player1, List.of(ownCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castCryptChampion(false);
        resolveAllTriggers();

        assertThat(findPermanent(player1, ownCreature.getName()).getCard()).isSameAs(ownCreature);
        assertThat(findPermanent(player2, opponentCreature.getName()).getCard()).isSameAs(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(tooExpensive)
                .doesNotContain(ownCreature);
        harness.assertNotOnBattlefield(player1, "Crypt Champion");
        harness.assertInGraveyard(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Each player chooses one qualifying creature when multiple are available")
    void eachPlayerChoosesOneQualifyingCreatureWhenMultipleAreAvailable() {
        Card ownFirst = new RakdosIckspitter();
        Card ownChosen = new MinisterOfImpediments();
        Card opponentFirst = new RakdosIckspitter();
        Card opponentChosen = new MinisterOfImpediments();
        harness.setGraveyard(player1, List.of(ownFirst, ownChosen));
        harness.setGraveyard(player2, List.of(opponentFirst, opponentChosen));

        castCryptChampion(true);
        resolveAllTriggers();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleGraveyardCardChosen(player2, 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, ownChosen.getName()).getCard()).isSameAs(ownChosen);
        assertThat(findPermanent(player2, opponentChosen.getName()).getCard()).isSameAs(opponentChosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownFirst);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentFirst);
        harness.assertOnBattlefield(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Does not allow a player to decline returning an eligible creature")
    void doesNotAllowDecliningAnEligibleCreature() {
        harness.setGraveyard(player1, List.of(new RakdosIckspitter(), new MinisterOfImpediments()));
        harness.setGraveyard(player2, List.of(new RakdosIckspitter()));

        castCryptChampion(true);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return noncreature or over-cost creature cards")
    void doesNotReturnNoncreatureOrOverCostCreatureCards() {
        Card nonCreature = new OcularHalo();
        Card tooExpensive = new TransguildCourier();
        harness.setGraveyard(player1, List.of(nonCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(new OcularHalo()));

        castCryptChampion(true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreature, tooExpensive);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, nonCreature.getName());
        harness.assertNotOnBattlefield(player1, tooExpensive.getName());
        harness.assertOnBattlefield(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Survives with red mana spent even when both graveyards are empty")
    void survivesWithRedManaAndEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castCryptChampion(true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Crypt Champion");
        harness.assertNotInGraveyard(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Entering without being cast still returns creatures and sacrifices the Champion")
    void enteringWithoutBeingCastReturnsCreaturesAndSacrificesChampion() {
        Card ownCreature = new RakdosIckspitter();
        Card opponentCreature = new MinisterOfImpediments();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        harness.enterBattlefieldAndReturn(player1, new CryptChampion());
        resolveAllTriggers();

        assertThat(findPermanent(player1, ownCreature.getName()).getCard()).isSameAs(ownCreature);
        assertThat(findPermanent(player2, opponentCreature.getName()).getCard()).isSameAs(opponentCreature);
        harness.assertNotOnBattlefield(player1, "Crypt Champion");
        harness.assertInGraveyard(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("The return and sacrifice abilities resolve separately with priority between them")
    void enterAbilitiesResolveSeparately() {
        Card ownCreature = new RakdosIckspitter();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of());

        castCryptChampion(false);
        harness.passBothPriorities();

        boolean championStillOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard() instanceof CryptChampion);
        boolean creatureStillInGraveyard = gd.playerGraveyards.get(player1.getId()).contains(ownCreature);
        assertThat(championStillOnBattlefield || creatureStillInGraveyard).isTrue();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanent(player1, ownCreature.getName()).getCard()).isSameAs(ownCreature);
        harness.assertNotOnBattlefield(player1, "Crypt Champion");
        harness.assertInGraveyard(player1, "Crypt Champion");
    }

    private void castCryptChampion(boolean spendRedMana) {
        harness.castFromHand(player1, new CryptChampion(), spendRedMana ? "{2}{B}{R}" : "{3}{B}");
        harness.passBothPriorities();
    }
}
