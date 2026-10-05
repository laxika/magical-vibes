package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadziStewardOfFateOraclesGift.class, Island.class, GrizzlyBears.class, VedalkenOrrery.class})
class JadziStewardOfFateOraclesGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Jadzi draws two cards, discards two cards, and prepares it")
    void entersPreparedAndRummages() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jadzi = findPermanent(player1, "Jadzi, Steward of Fate");
        assertThat(jadzi.isPrepared()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting Oracle's Gift creates X Fractals and puts X counters on each")
    void createsFractalsWithXCounters() {
        Permanent jadzi = castJadzi();
        UUID copyId = jadzi.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCardFromExile(gd, player1, copyId, 2, null);
        harness.passBothPriorities();

        List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FRACTAL))
                .toList();
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(fractal.getEffectivePower()).isEqualTo(2);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(jadzi.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Jadzi is prepared before its draw and discard trigger resolves")
    void preparedImmediatelyOnEntry() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent jadzi = findPermanent(player1, "Jadzi, Steward of Fate");
        assertThat(jadzi.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(jadzi.getPreparedSpellCardId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Jadzi can discard the two cards it just drew from an empty hand")
    void discardsNewlyDrawnCards() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Jadzi, Steward of Fate").isPrepared()).isTrue();
    }

    @Test
    @DisplayName("Oracle's Gift with X zero creates no tokens but unprepares Jadzi when cast")
    void zeroXUnpreparesWithoutCreatingTokens() {
        Permanent jadzi = castJadzi();
        UUID copyId = jadzi.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCardFromExile(gd, player1, copyId, 0, null);

        assertThat(jadzi.isPrepared()).isFalse();
        assertThat(jadzi.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(jadzi);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Oracle's Gift adds counters to existing controlled Fractals but not opposing Fractals or Jadzi")
    void countersIncludeExistingFractalsAndRespectController() {
        harness.forceActivePlayer(player2);
        Permanent opposingJadzi = castJadzi(player2);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        gs.playCardFromExile(gd, player2, opposingJadzi.getPreparedSpellCardId(), 2, null);
        harness.passBothPriorities();
        Permanent opposingFractal = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        Permanent existingFractal = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken() && permanent != opposingFractal)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player2.getId()).remove(existingFractal);
        gd.playerBattlefields.get(player1.getId()).add(existingFractal);

        harness.forceActivePlayer(player1);
        Permanent jadzi = castJadzi();
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCardFromExile(gd, player1, jadzi.getPreparedSpellCardId(), 2, null);
        harness.passBothPriorities();

        assertThat(existingFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opposingFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(jadzi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(3);
    }

    @Test
    @DisplayName("Resolving Jadzi's entry trigger does not prepare it again after its spell was cast in response")
    void entryTriggerDoesNotPrepareAgain() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JadziStewardOfFateOraclesGift()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent jadzi = findPermanent(player1, "Jadzi, Steward of Fate");

        gs.playCardFromExile(gd, player1, jadzi.getPreparedSpellCardId(), 0, null);
        harness.passBothPriorities();
        assertThat(jadzi.isPrepared()).isFalse();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(jadzi.isPrepared()).isFalse();
        assertThat(jadzi.getPreparedSpellCardId()).isNull();
    }

    private Permanent castJadzi() {
        return castJadzi(player1);
    }

    private Permanent castJadzi(Player player) {
        harness.setLibrary(player, List.of(new Island(), new Island()));
        harness.setHand(player, List.of(new JadziStewardOfFateOraclesGift(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player, 0);
        harness.handleCardChosen(player, 0);
        return findPermanent(player, "Jadzi, Steward of Fate");
    }

}
