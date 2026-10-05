package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.t.TippyToeTerrificPartner;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overencumbered.class, SuntailHawk.class, SolemnSimulacrum.class,
        ErdwalIlluminator.class, TippyToeTerrificPartner.class})
class OverencumberedTest extends BaseCardTest {

    @Test
    @DisplayName("When Overencumbered enters, the enchanted opponent creates a Clue, Food, and Junk")
    void createsArtifactTokensForEnchantedOpponent() {
        attachOverencumbered();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .contains("Clue", "Food", "Junk");
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.CLUE)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.FOOD)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                p -> p.getCard().getSubtypes().contains(CardSubtype.JUNK)).hasSize(1);
    }

    @Test
    @DisplayName("Declining the artifact payment prevents creatures from attacking this combat")
    void decliningPaymentPreventsAttacks() {
        attachOverencumbered();
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        choosePayment(false);

        assertThat(als.canAttack(gd, findPermanent(player2, "Suntail Hawk"), player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Paying for all controlled artifacts allows attacks")
    void payingForArtifactsAllowsAttacks() {
        attachOverencumbered();
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        choosePayment(true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        declareAttackers(player2);
    }

    @Test
    @DisplayName("Overencumbered cannot enchant its controller")
    void cannotEnchantController() {
        harness.setHand(player1, List.of(new Overencumbered()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifacts are counted when the combat ability resolves")
    void countsArtifactsAfterResponsesToCombatTrigger() {
        attachOverencumbered();
        harness.setLibrary(player2, List.of(new Overencumbered()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activateToken("Clue");
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Clue")).isZero();
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        choosePayment(true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        Permanent creature = addCreatureReady(player2, new SolemnSimulacrum());
        assertThat(als.canAttack(gd, creature, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("The three tokens are created in a single event for token replacements")
    void createsAllThreeTokensInOneEvent() {
        harness.addToBattlefield(player2, new TippyToeTerrificPartner());

        attachOverencumbered();

        assertThat(countPermanents(player2, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isEqualTo(2);
        assertThat(countPermanents(player2, "Junk")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creating the Clue is not investigating")
    void clueCreationDoesNotTriggerInvestigateAbilities() {
        harness.addToBattlefield(player2, new ErdwalIlluminator());

        attachOverencumbered();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Clue")).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact creatures count toward the payment, but the Aura controller's artifacts do not")
    void countsOnlyEnchantedPlayersArtifactsIncludingArtifactCreatures() {
        attachOverencumbered();
        Permanent creature = addCreatureReady(player2, new SolemnSimulacrum());
        harness.addToBattlefield(player1, new SolemnSimulacrum());

        resolveCombatTrigger(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        choosePayment(true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(als.canAttack(gd, creature, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("An unaffordable payment prevents attacks without spending partial mana")
    void insufficientManaPreventsAttacks() {
        attachOverencumbered();
        Permanent creature = addCreatureReady(player2, new SolemnSimulacrum());

        resolveCombatTrigger(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        choosePayment(true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(als.canAttack(gd, creature, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("With no artifacts the opponent may pay zero and allow attacks")
    void zeroArtifactPaymentAllowsAttacks() {
        attachOverencumbered();
        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent creature = addCreatureReady(player2, new ErdwalIlluminator());

        resolveCombatTrigger(player2);
        choosePayment(true);

        assertThat(als.canAttack(gd, creature, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("The opponent may decline even a zero payment")
    void decliningZeroPaymentStillPreventsAttacks() {
        attachOverencumbered();
        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent creature = addCreatureReady(player2, new ErdwalIlluminator());

        resolveCombatTrigger(player2);
        choosePayment(false);

        assertThat(als.canAttack(gd, creature, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The Aura does not demand a payment on its controller's turn")
    void noPaymentOnControllersTurn() {
        attachOverencumbered();
        Permanent creature = addCreatureReady(player1, new SolemnSimulacrum());

        resolveCombatTrigger(player1);

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueTokenDrawsCard() {
        attachOverencumbered();
        Overencumbered drawnCard = new Overencumbered();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activateToken("Clue");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Clue")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(drawnCard);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The created Food can be sacrificed for two mana to gain three life")
    void foodTokenGainsLife() {
        attachOverencumbered();
        harness.setLife(player2, 10);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activateToken("Food");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Food")).isZero();
        harness.assertLife(player2, 13);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The created Junk exiles its controller's top card and lets them cast it by paying its mana cost")
    void junkTokenExilesAndAllowsPaidCasting() {
        attachOverencumbered();
        Overencumbered exiledCard = new Overencumbered();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);

        activateToken("Junk");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Junk")).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThatThrownBy(() -> harness.castFromExile(player2, exiledCard.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, exiledCard.getId(), player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Overencumbered");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Junk cannot be activated outside sorcery timing")
    void junkCannotBeActivatedDuringOpponentsTurn() {
        attachOverencumbered();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> activateToken("Junk")).isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Junk")).isEqualTo(1);
    }

    private void activateToken(String name) {
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(findPermanent(player2, name));
        harness.ensurePriority(player2);
        harness.activateAbility(player2, index, null, null);
    }

    private void choosePayment(boolean accepted) {
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handleMayAbilityChosen(player2, accepted));
    }

    private void attachOverencumbered() {
        harness.setHand(player1, List.of(new Overencumbered()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveCombatTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void declareAttackers(Player attacker) {
        List<Permanent> battlefield = gd.playerBattlefields.get(attacker.getId());
        int attackerIndex = IntStream.range(0, battlefield.size())
                .filter(i -> battlefield.get(i).getCard() instanceof SuntailHawk)
                .findFirst()
                .orElseThrow();
        declareAttackers(attacker, List.of(attackerIndex));
    }
}
