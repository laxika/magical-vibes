package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stormsplitter.class, Shock.class, Divination.class, GrizzlyBears.class})
class StormsplitterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a token copy")
    void instantCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery creates a token copy")
    void sorceryCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not create a token copy")
    void creatureDoesNotCreateTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countTokens()).isZero();
    }

    @Test
    @DisplayName("The token copy is exiled at the next end step")
    void tokenCopyIsExiledAtNextEndStep() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countTokens()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countTokens()).isZero();
    }

    @Test
    @DisplayName("Token copies trigger on later spells and all copies are exiled")
    void tokenCopiesCreateMoreCopies() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countTokens()).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countTokens()).isEqualTo(3);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(countTokens()).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countTokens()).isZero();
        harness.assertOnBattlefield(player1, "Stormsplitter");
    }

    @Test
    @DisplayName("A copy does not trigger retroactively for spells already cast")
    void unresolvedTriggersDoNotDoubleCopies() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Stormsplitter")
    void opponentsSpellDoesNotCreateCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countTokens()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A copy still enters when Stormsplitter dies before the trigger resolves")
    void copyCreatedAfterSourceDies() {
        harness.addToBattlefield(player1, new Stormsplitter());
        var sourceId = harness.getPermanentId(player1, "Stormsplitter");
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Stormsplitter");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Stormsplitter");
        harness.assertInGraveyard(player1, "Stormsplitter");
    }

    @Test
    @DisplayName("The copy does not copy tapped status or counters")
    void copyDoesNotCopyTappedStatusOrCounters() {
        harness.addToBattlefield(player1, new Stormsplitter());
        var source = findPermanent(player1, "Stormsplitter");
        source.tap();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Copies made during an end step survive until the following end step")
    void endStepCopyWaitsForFollowingEndStep() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countTokens()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(countTokens()).isEqualTo(1);
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(countTokens()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(countTokens()).isZero();
        harness.assertOnBattlefield(player1, "Stormsplitter");
    }

    private long countTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
