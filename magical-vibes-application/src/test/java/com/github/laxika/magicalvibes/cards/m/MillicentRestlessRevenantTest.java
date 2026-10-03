package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoicelessSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MillicentRestlessRevenant.class, VoicelessSpirit.class, GrizzlyBears.class, Murder.class})
class MillicentRestlessRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Spirits reduces Millicent's generic cost")
    void affinityForSpiritsReducesGenericCost() {
        harness.addToBattlefield(player1, new VoicelessSpirit());
        harness.setHand(player1, List.of(new MillicentRestlessRevenant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Millicent and another nontoken Spirit dealing combat damage create Spirits")
    void ownAndAnotherNontokenSpiritCombatDamageCreateSpirits() {
        addCreatureReady(player1, new MillicentRestlessRevenant()).setAttacking(true);
        addCreatureReady(player1, new VoicelessSpirit()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("A non-Spirit dealing combat damage does not create a Spirit")
    void nonSpiritCombatDamageDoesNotCreateSpirit() {
        addCreatureReady(player1, new MillicentRestlessRevenant());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Another nontoken Spirit dying creates a Spirit")
    void anotherNontokenSpiritDyingCreatesSpirit() {
        addCreatureReady(player1, new MillicentRestlessRevenant());
        Permanent spirit = addCreatureReady(player1, new VoicelessSpirit());

        destroyWithMurder(spirit);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Millicent dying creates a Spirit")
    void millicentDyingCreatesSpirit() {
        Permanent millicent = addCreatureReady(player1, new MillicentRestlessRevenant());

        destroyWithMurder(millicent);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A Spirit token dying does not trigger Millicent")
    void spiritTokenDyingDoesNotTriggerMillicent() {
        addCreatureReady(player1, new MillicentRestlessRevenant());
        Permanent nontokenSpirit = addCreatureReady(player1, new VoicelessSpirit());
        destroyWithMurder(nontokenSpirit);

        Permanent token = findPermanents(player1, "Spirit").getFirst();
        token.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void destroyWithMurder(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }
}
