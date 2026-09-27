package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpawningKraken.class, GrizzlyBears.class})
class SpawningKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("A Kraken, Leviathan, Octopus, or Serpent dealing combat damage creates a 9/9 blue Kraken")
    void seaMonsterCombatDamageCreatesKrakenToken() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReadyWithSubtype(CardSubtype.KRAKEN);
        addCreatureReadyWithSubtype(CardSubtype.LEVIATHAN);
        addCreatureReadyWithSubtype(CardSubtype.OCTOPUS);
        addCreatureReadyWithSubtype(CardSubtype.SERPENT);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2, 3, 4, 5));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .allMatch(permanent -> permanent.getCard().getName().equals("Kraken")
                        && permanent.getCard().getPower() == 9
                        && permanent.getCard().getToughness() == 9
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getSubtypes().contains(CardSubtype.KRAKEN)))
                .isTrue();
    }

    @Test
    @DisplayName("A non-sea-monster dealing combat damage does not trigger Spawning Kraken")
    void nonSeaMonsterCombatDamageDoesNotCreateToken() {
        addCreatureReady(player1, new SpawningKraken());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }

    private Permanent addCreatureReadyWithSubtype(CardSubtype subtype) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(subtype));
        return addCreatureReady(player1, card);
    }
}
