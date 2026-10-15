package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.o.Oust;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuktukTheExplorer.class, WrathOfGod.class, FlameSlash.class, Oust.class})
class TuktukTheExplorerTest extends BaseCardTest {

    @Test
    void deathTriggerCreatesLegendaryArtifactCreatureToken() {
        harness.addToBattlefield(player1, new TuktukTheExplorer());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tuktuk the Explorer");
        Permanent token = findPermanent(player1, "Tuktuk the Returned");
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.GOBLIN, CardSubtype.GOLEM);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void deathTriggerWaitsForResolutionAndCreatesTokenForDyingCreaturesController() {
        Permanent tuktuk = harness.addToBattlefieldAndReturn(player2, new TuktukTheExplorer());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, tuktuk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tuktuk the Explorer");
        harness.assertNotOnBattlefield(player1, "Tuktuk the Returned");
        harness.assertNotOnBattlefield(player2, "Tuktuk the Returned");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tuktuk the Returned");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = findPermanent(player2, "Tuktuk the Returned");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCard().getKeywords()).isEmpty();
    }

    @Test
    void puttingTuktukIntoLibraryDoesNotCreateToken() {
        Permanent tuktuk = harness.addToBattlefieldAndReturn(player1, new TuktukTheExplorer());
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, tuktuk.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tuktuk the Explorer");
        harness.assertNotOnBattlefield(player1, "Tuktuk the Returned");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tuktuk the Explorer"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hasteAllowsAttackingTheTurnTuktukEnters() {
        harness.castFromHand(player1, new TuktukTheExplorer(), "{2}{R}");
        resolveAllTriggers();
        Permanent tuktuk = findPermanent(player1, "Tuktuk the Explorer");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(tuktuk.isAttacking()).isTrue();
    }
}
