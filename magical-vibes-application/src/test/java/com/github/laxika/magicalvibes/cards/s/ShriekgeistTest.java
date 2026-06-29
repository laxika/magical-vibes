package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShriekgeistTest extends BaseCardTest {

    private Permanent addReadyShriekgeist() {
        Permanent perm = new Permanent(new Shriekgeist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.put(player2.getId(), new ArrayList<>(cards));
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Has combat damage mill effect for 2 cards")
    void hasCorrectEffect() {
        Shriekgeist card = new Shriekgeist();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);

        MillTargetPlayerEffect mill =
                (MillTargetPlayerEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(mill.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Dealing combat damage mills 2 cards")
    void millsTwoCardsOnCombatDamage() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Serra Angel");
    }

    @Test
    @DisplayName("Milled cards go to graveyard from the top of library")
    void milledCardsGoToGraveyard() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        List<Card> graveyard = gd.playerGraveyards.get(player2.getId());
        assertThat(graveyard).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Serra Angel");
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Shriekgeist")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk()
        ));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No trigger when Shriekgeist is blocked")
    void noTriggerWhenBlocked() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        setDeck(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Handles library with fewer than 2 cards")
    void partialLibraryMill() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        setDeck(List.of(new GrizzlyBears()));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles empty library gracefully")
    void emptyLibrary() {
        Permanent shriekgeist = addReadyShriekgeist();
        shriekgeist.setAttacking(true);

        setDeck(List.of());

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
