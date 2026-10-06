package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrynnChampionOfFreedom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilvarDevourerOfTheFree.class, GrizzlyBears.class, TrynnChampionOfFreedom.class})
class SilvarDevourerOfTheFreeTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Trynn")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card partner = namedCard("Trynn, Champion of Freedom");
        Card decoy = new GrizzlyBears();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.setHand(player1, List.of(new SilvarDevourerOfTheFree()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Trynn, Champion of Freedom");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Trynn, Champion of Freedom");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("Sacrificing a Human puts a counter on Silvar and grants indestructible")
    void sacrificeHumanPutsCounterAndGrantsIndestructible() {
        Permanent silvar = addReadySilvar();
        harness.addToBattlefield(player1, createHuman());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Human");
        assertThat(silvar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(silvar.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The ability cannot sacrifice a non-Human creature")
    void abilityRequiresHuman() {
        addReadySilvar();
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleResetsAtEndOfTurn() {
        Permanent silvar = addReadySilvar();
        harness.addToBattlefield(player1, createHuman());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(silvar.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(silvar.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    private Permanent addReadySilvar() {
        return addCreatureReady(player1, new SilvarDevourerOfTheFree());
    }

    @Test
    void partnerSearchCanFindRealTrynnInControllersLibrary() {
        Card partner = new TrynnChampionOfFreedom();
        harness.setLibrary(player1, List.of(partner));
        harness.setHand(player1, List.of(new SilvarDevourerOfTheFree()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(partner);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        Card partner = new TrynnChampionOfFreedom();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(partner));
        harness.setHand(player1, List.of(new SilvarDevourerOfTheFree()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(partner);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void summoningSickSilvarPaysSacrificeBeforeReceivingEffects() {
        Permanent silvar = harness.addToBattlefieldAndReturn(player1, new SilvarDevourerOfTheFree());
        silvar.setSummoningSick(true);
        harness.addToBattlefield(player1, new TrynnChampionOfFreedom());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Trynn, Champion of Freedom");
        harness.assertNotOnBattlefield(player1, "Trynn, Champion of Freedom");
        assertThat(silvar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(silvar.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);

        harness.passBothPriorities();

        assertThat(silvar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(silvar.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void cannotSacrificeOpponentsHuman() {
        addReadySilvar();
        harness.addToBattlefield(player2, new TrynnChampionOfFreedom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Trynn, Champion of Freedom");
    }

    private Card createHuman() {
        Card card = new Card();
        card.setName("Human");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
