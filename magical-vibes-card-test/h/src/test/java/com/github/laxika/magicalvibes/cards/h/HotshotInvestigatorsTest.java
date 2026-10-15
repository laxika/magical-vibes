package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.r.RedHerring;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotshotInvestigators.class, RedHerring.class, Plains.class, GlazeFiend.class})
class HotshotInvestigatorsTest extends BaseCardTest {

    @Test
    void returnsCreatureYouControlAndInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        castHotshot(target.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof RedHerring);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void returnsOpponentsCreatureWithoutInvestigating() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        castHotshot(target.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof RedHerring);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void canDeclineOptionalTarget() {
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Hotshot Investigators")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void returnsCreatureBeforeCreatingClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Glaze Fiend");
        harness.assertNotOnBattlefield(player1, "Glaze Fiend");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenTargetIsSacrificedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Red Herring");
        harness.assertNotInHand(player1, "Red Herring");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void canDeclineTargetEvenWhenAnotherCreatureIsAvailable() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void canReturnAnotherHotshotInvestigators() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HotshotInvestigators());
        castHotshot(other.getId());

        harness.assertInHand(player1, "Hotshot Investigators");
        assertThat(findPermanents(player1, "Hotshot Investigators")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    private void castHotshot(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
