package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.s.SwornCompanions;
import com.github.laxika.magicalvibes.cards.v.VizierOfManyFaces;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineVisitation.class, BladeSplicer.class, SwornCompanions.class, VizierOfManyFaces.class})
class DivineVisitationTest extends BaseCardTest {

    @Test
    void replacesCreatureTokensWithAngels() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Angel");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(token.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void doesNotReplaceOpponentsCreatureTokens() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).isEmpty();
    }

    @Test
    void replacesEveryTokenAndRemovesOriginalTokenAbilities() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.castFromHand(player1, new SwornCompanions(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player1, "Angel")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getKeywords())
                    .containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
        });
    }

    @Test
    void multipleVisitationsDoNotMultiplyTokens() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.castFromHand(player1, new SwornCompanions(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void doesNotChangeTokensCreatedBeforeVisitationEnters() {
        harness.castFromHand(player1, new SwornCompanions(), "{2}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DivineVisitation(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void doesNotReplaceTokensWhenVisitationLeavesBeforeCreation() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.castFromHand(player1, new SwornCompanions(), "{2}{W}");
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof DivineVisitation);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void replacesEmbalmedCloneBeforeItsCopyOnEntryChoice() {
        harness.addToBattlefield(player1, new DivineVisitation());
        harness.addToBattlefield(player1, new BladeSplicer());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Angel")).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
            assertThat(token.getCard().getKeywords())
                    .containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
        });
    }
}
