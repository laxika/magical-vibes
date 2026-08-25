package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootPilgrimage.class, AdaptiveGemguard.class, CoralMerfolk.class, GrizzlyBears.class})
class DeeprootPilgrimageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hexproof Merfolk token when a nontoken Merfolk becomes tapped")
    void createsTokenWhenNontokenMerfolkBecomesTapped() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent merfolk = addReady(player1, new CoralMerfolk());

        tapAndCheckTriggers(merfolk);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Merfolk").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MERFOLK);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a token or a non-Merfolk permanent")
    void ignoresTokensAndNonMerfolk() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent merfolk = addReady(player1, new CoralMerfolk());
        Permanent bear = addReady(player1, new GrizzlyBears());

        tapAndCheckTriggers(merfolk);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Merfolk").getFirst();

        tapAndCheckTriggers(token);
        tapAndCheckTriggers(bear);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers only once when two Merfolk are tapped together to pay one cost")
    void triggersOnceForSimultaneousTaps() {
        harness.addToBattlefield(player1, new DeeprootPilgrimage());
        Permanent gemguard = addReady(player1, new AdaptiveGemguard());
        Permanent firstMerfolk = addReady(player1, new CoralMerfolk());
        Permanent secondMerfolk = addReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gemguard), null, null);
        harness.handlePermanentChosen(player1, firstMerfolk.getId());
        harness.handlePermanentChosen(player1, secondMerfolk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void tapAndCheckTriggers(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
