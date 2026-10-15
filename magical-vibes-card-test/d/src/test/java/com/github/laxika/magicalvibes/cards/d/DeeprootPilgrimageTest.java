package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldScout;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootPilgrimage.class, AdaptiveGemguard.class, RiverHeraldScout.class})
class DeeprootPilgrimageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hexproof Merfolk token when a nontoken Merfolk becomes tapped")
    void createsTokenWhenNontokenMerfolkBecomesTapped() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent merfolk = addCreatureReady(player1, new RiverHeraldScout());

        tapAndCheckTriggers(merfolk);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Merfolk").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MERFOLK);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a token or a non-Merfolk permanent")
    void ignoresTokensAndNonMerfolk() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent merfolk = addCreatureReady(player1, new RiverHeraldScout());
        Permanent nonMerfolk = addCreatureReady(player1, new AdaptiveGemguard());

        tapAndCheckTriggers(merfolk);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Merfolk").getFirst();

        tapAndCheckTriggers(token);
        tapAndCheckTriggers(nonMerfolk);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers only once when two Merfolk are tapped together to pay one cost")
    void triggersOnceForSimultaneousTaps() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent gemguard = addCreatureReady(player1, new AdaptiveGemguard());
        Permanent firstMerfolk = addCreatureReady(player1, new RiverHeraldScout());
        Permanent secondMerfolk = addCreatureReady(player1, new RiverHeraldScout());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gemguard), null, null);
        harness.handlePermanentChosen(player1, firstMerfolk.getId());
        harness.handlePermanentChosen(player1, secondMerfolk.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking with multiple nontoken Merfolk creates only one token")
    void simultaneousAttackingMerfolkCreateOneToken() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent first = addCreatureReady(player1, new RiverHeraldScout());
        Permanent second = addCreatureReady(player1, new RiverHeraldScout());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Separate Merfolk tap events each create a token")
    void separateTapEventsCreateSeparateTokens() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent first = addCreatureReady(player1, new RiverHeraldScout());
        Permanent second = addCreatureReady(player1, new RiverHeraldScout());

        tapAndCheckTriggers(first);
        tapAndCheckTriggers(second);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(2);
    }

    @Test
    @DisplayName("Opponent's Merfolk do not trigger Pilgrimage")
    void opposingMerfolkDoNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent opposingMerfolk = addCreatureReady(player2, new RiverHeraldScout());

        tapAndCheckTriggers(opposingMerfolk);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).isEmpty();
        assertThat(findPermanents(player2, "Merfolk")).isEmpty();
    }

    @Test
    @DisplayName("Each Pilgrimage triggers independently for the same simultaneous taps")
    void multiplePilgrimagesEachCreateToken() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent gemguard = addCreatureReady(player1, new AdaptiveGemguard());
        Permanent first = addCreatureReady(player1, new RiverHeraldScout());
        Permanent second = addCreatureReady(player1, new RiverHeraldScout());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gemguard), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(2);
    }

    private void tapAndCheckTriggers(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
