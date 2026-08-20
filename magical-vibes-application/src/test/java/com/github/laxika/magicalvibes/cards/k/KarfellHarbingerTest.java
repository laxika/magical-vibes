package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarfellHarbingerTest extends BaseCardTest {

    @Test
    void addsManaThatCanForetellFromHand() {
        Permanent harbinger = addReadyHarbinger();
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getForetellOrInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
        assertThat(harbinger.isTapped()).isTrue();
    }

    @Test
    void addsManaThatCanCastInstantOrSorcery() {
        addReadyHarbinger();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void manaCannotCastCreatureSpell() {
        addReadyHarbinger();
        harness.setHand(player1, List.of(new AuguryRaven()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getForetellOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyHarbinger() {
        harness.addToBattlefield(player1, new KarfellHarbinger());
        Permanent harbinger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harbinger.setSummoningSick(false);
        return harbinger;
    }
}
