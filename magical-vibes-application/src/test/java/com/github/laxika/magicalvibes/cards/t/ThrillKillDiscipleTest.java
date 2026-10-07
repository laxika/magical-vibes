package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrillKillDisciple.class, Forest.class})
class ThrillKillDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Squad discards one card and creates one copy per payment")
    void squadDiscardsAndCreatesCopies() {
        harness.setHand(player1, List.of(new ThrillKillDisciple(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCostsAndDiscards(player1, 0, List.of("{1}"), List.of(1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Thrill-Kill Disciple")).isEqualTo(2);
        assertThat(findPermanents(player1, "Thrill-Kill Disciple"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Dying creates a Junk artifact token")
    void deathCreatesJunk() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new ThrillKillDisciple());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, disciple));
        harness.passBothPriorities();

        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(junk.getCard().getSubtypes()).contains(CardSubtype.JUNK);
    }

    @Test
    @DisplayName("Squad does not trigger when its cost was not paid")
    void unpaidSquadDoesNotTrigger() {
        harness.setHand(player1, List.of(new ThrillKillDisciple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thrill-Kill Disciple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two squad payments discard two cards and the copies do not trigger squad")
    void multipleSquadPaymentsAndCopies() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new ThrillKillDisciple(), first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCostsAndDiscards(player1, 0,
                List.of("{1}", "{1}"), List.of(1, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thrill-Kill Disciple")).hasSize(3);
        assertThat(findPermanents(player1, "Thrill-Kill Disciple"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each squad payment requires a different discarded card")
    void squadCannotDiscardTheSameCardTwice() {
        harness.setHand(player1, List.of(new ThrillKillDisciple(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCostsAndDiscards(player1, 0,
                List.of("{1}", "{1}"), List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Thrill-Kill Disciple");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A squad token copy also creates Junk when it dies")
    void tokenCopyCreatesJunkOnDeath() {
        harness.setHand(player1, List.of(new ThrillKillDisciple(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithRepeatedCostsAndDiscards(player1, 0, List.of("{1}"), List.of(1));
        resolveAllTriggers();
        Permanent copy = findPermanents(player1, "Thrill-Kill Disciple").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, copy));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).hasSize(1);
        assertThat(findPermanents(player1, "Thrill-Kill Disciple")).hasSize(1);
    }

    @Test
    @DisplayName("Junk sacrifices itself, exiles only the top card, and allows playing a land")
    void junkAllowsPlayingLand() {
        createJunk();
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));

        activateJunk();
        assertThat(findPermanents(player1, "Junk")).isEmpty();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        harness.castFromExile(player1, top.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Junk does not let an exiled spell be cast for free")
    void junkRequiresMana() {
        createJunk();
        ThrillKillDisciple top = new ThrillKillDisciple();
        harness.setLibrary(player1, List.of(top));
        activateJunk();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, top.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Thrill-Kill Disciple");
    }

    @Test
    @DisplayName("Junk cannot be activated during combat")
    void junkRequiresSorceryTiming() {
        createJunk();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(this::activateJunk).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Junk play permission expires at the end of the turn")
    void junkPermissionExpires() {
        createJunk();
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        activateJunk();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    private void createJunk() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new ThrillKillDisciple());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, disciple));
        resolveAllTriggers();
    }

    private void activateJunk() {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Junk"));
        harness.activateAbility(player1, index, null, null);
    }
}
