package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shriekgeist.class, StormboundGeist.class, SilverclawGriffin.class})
class ShriekgeistTest extends BaseCardTest {

    private Permanent addReadyShriekgeist() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new Shriekgeist());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Dealing combat damage mills 2 cards")
    void millsTwoCardsOnCombatDamage() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new StormboundGeist(),
                new StormboundGeist(),
                new SilverclawGriffin(),
                new Shriekgeist()
        ));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Silverclaw Griffin");
    }

    @Test
    @DisplayName("Milled cards go to graveyard from the top of library")
    void milledCardsGoToGraveyard() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new StormboundGeist(),
                new SilverclawGriffin(),
                new Shriekgeist(),
                new StormboundGeist()
        ));

        resolveCombat();
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player2.getId());
        assertThat(graveyard).extracting(Card::getName)
                .containsExactly("Stormbound Geist", "Silverclaw Griffin");
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Shriekgeist")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        harness.setLibrary(player2, List.of(
                new StormboundGeist(),
                new StormboundGeist(),
                new SilverclawGriffin(),
                new Shriekgeist()
        ));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No trigger when Shriekgeist is blocked")
    void noTriggerWhenBlocked() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new StormboundGeist());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLibrary(player2, List.of(
                new StormboundGeist(),
                new StormboundGeist(),
                new SilverclawGriffin(),
                new Shriekgeist()
        ));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Handles library with fewer than 2 cards")
    void partialLibraryMill() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        harness.setLibrary(player2, List.of(new StormboundGeist()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles empty library gracefully")
    void emptyLibrary() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        harness.setLibrary(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mill waits for the trigger to resolve and survives the source leaving")
    void millResolvesAfterSourceLeaves() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);
        Card first = new StormboundGeist();
        Card second = new SilverclawGriffin();
        Card remaining = new Shriekgeist();
        harness.setLibrary(player2, List.of(first, second, remaining));
        harness.setLibrary(player1, List.of(new StormboundGeist()));

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, remaining);

        gd.playerBattlefields.get(player1.getId()).remove(shriekgeist);
        gd.playerGraveyards.get(player1.getId()).add(shriekgeist.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Shriekgeist that damages a player triggers separately")
    void multipleShriekgeistsEachMillTwo() {
        addReadyShriekgeist().setAttacking(true);
        addReadyShriekgeist().setAttacking(true);
        harness.setLibrary(player2, List.of(new StormboundGeist(), new StormboundGeist(),
                new SilverclawGriffin(), new SilverclawGriffin(), new Shriekgeist()));

        resolveCombat();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
