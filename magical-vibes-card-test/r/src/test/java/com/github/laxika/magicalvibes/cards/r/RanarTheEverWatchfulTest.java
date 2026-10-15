package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.e.Expel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RanarTheEverWatchful.class, AuguryRaven.class, Expel.class, GrizzlyBears.class})
class RanarTheEverWatchfulTest extends BaseCardTest {

    @Test
    void makesFirstForetellEachTurnFreeAndCreatesSpirit() {
        addCreatureReady(player1, new RanarTheEverWatchful());
        Card first = new AuguryRaven();
        Card second = new AuguryRaven();
        harness.setHand(player1, List.of(first, second));

        harness.foretell(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void createsSpiritWhenYourSpellExilesABattlefieldPermanent() {
        addCreatureReady(player1, new RanarTheEverWatchful());
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }
}
