package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingImitation.class, AirElemental.class, Forest.class, GrizzlyBears.class, Island.class})
class HauntingImitationTest extends BaseCardTest {

    @Test
    void createsA1x1FlyingSpiritCopyForEachRevealedCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        AirElemental elemental = new AirElemental();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of(elemental));
        HauntingImitation spell = new HauntingImitation();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Air Elemental");
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        }
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void returnsToHandIfNoCreatureWasRevealed() {
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(island));
        HauntingImitation spell = new HauntingImitation();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(island);
    }
}
