package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfTheCanonized.class, DoomedTraveler.class, FesteringGoblin.class})
class MarchOfTheCanonizedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X white lifelink Vampire tokens when it enters")
    void etbCreatesXWhiteLifelinkVampires() {
        harness.setHand(player1, List.of(new MarchOfTheCanonized()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = tokensNamed(player1, "Vampire");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates a flying Vampire Demon when combined white and black devotion reaches seven")
    void upkeepCreatesVampireDemonAtThreshold() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = tokensNamed(player1, "Vampire Demon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Vampire Demon below seven combined devotion")
    void upkeepDoesNotCreateVampireDemonBelowThreshold() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(tokensNamed(player1, "Vampire Demon")).isEmpty();
    }

    private List<Permanent> tokensNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .toList();
    }
}
