package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScouringSwarm.class, ZuranOrb.class, Forest.class})
class ScouringSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land creates a tapped flying Insect before threshold")
    void createsInsectBeforeSevenLandCardsInGraveyard() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new ScouringSwarm());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Insect").getFirst();
        assertThat(token.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Seven land cards make the trigger create a tapped token copy")
    void createsTappedTokenCopyAtSevenLandCards() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new ScouringSwarm());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Scouring Swarm")).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
    }
}
