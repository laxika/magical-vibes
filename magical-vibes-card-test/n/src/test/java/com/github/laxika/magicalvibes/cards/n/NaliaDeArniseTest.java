package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaliaDeArnise.class, BoggartBrute.class, FugitiveWizard.class,
        GrizzlyBears.class, SoulWarden.class})
class NaliaDeArniseTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a Wizard spell from the top of the library")
    void castsPartySpellFromLibraryTop() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card wizard = new FugitiveWizard();
        gd.playerDecks.get(player1.getId()).addFirst(wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == wizard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(wizard);
    }

    @Test
    @DisplayName("Cannot cast a non-party creature from the top of the library")
    void cannotCastNonPartyCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("A full party puts counters on your creatures and grants deathtouch until end of turn")
    void fullPartyPutsCountersAndGrantsDeathtouch() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new BoggartBrute());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        for (Permanent creature : new Permanent[]{nalia, cleric, warrior, wizard}) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant the combat bonus without a full party")
    void doesNotGrantCombatBonusWithoutFullParty() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new BoggartBrute());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        for (Permanent creature : new Permanent[]{nalia, cleric, warrior, other}) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        }
    }

    @Test
    void castsClericAndWarriorFromLibraryTopInTheSameTurn() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card cleric = new SoulWarden();
        Card warrior = new BoggartBrute();
        harness.setLibrary(player1, List.of(cleric, warrior));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == cleric)
                .anyMatch(permanent -> permanent.getCard() == warrior);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canCastRogueFromLibraryTop() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card rogue = new NaliaDeArnise();
        harness.setLibrary(player1, List.of(rogue));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromLibraryTop(player1);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == rogue);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastPartySpellWithoutPayingMana() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card wizard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(wizard));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wizard);
    }

    @Test
    void cannotCastNonFlashPartySpellOnOpponentsTurn() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card wizard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(wizard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wizard);
    }

    @Test
    void privatelyShowsEvenNonPartyTopCard() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).allMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void naliaInLibraryDoesNotRevealHerself() {
        harness.setLibrary(player1, List.of(new NaliaDeArnise()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).isNotEmpty().allMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void losesLibraryPermissionWhenNaliaLeavesBattlefield() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        Card wizard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(wizard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(nalia);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wizard);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn1().getSentMessages()).allMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(nalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, nalia, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void losingPartyMemberBeforeResolutionPreventsEntireBonus() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(wizard);

        harness.passBothPriorities();

        assertThat(nalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, nalia, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void completingPartyAfterCombatBeginsDoesNotCreateTrigger() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.passBothPriorities();

        assertThat(nalia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, nalia, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void bonusAffectsNonPartyCreaturesButNotCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        advanceToCombat(player1);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(later.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, later, Keyword.DEATHTOUCH)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
