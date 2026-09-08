package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.s.SpittingEarth;
import com.github.laxika.magicalvibes.cards.s.StalkingTiger;
import com.github.laxika.magicalvibes.cards.u.UktabiFaerie;
import com.github.laxika.magicalvibes.cards.u.UnyaroBeeSting;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BayFalcon.class, CeruleanWyvern.class, SpittingEarth.class, UktabiFaerie.class,
        UnyaroBeeSting.class, ViashinoWarrior.class, StalkingTiger.class})
class CeruleanWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("A non-flying creature cannot block Cerulean Wyvern")
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new CeruleanWyvern());
        addCreatureReady(player2, new ViashinoWarrior());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A green creature cannot block Cerulean Wyvern")
    void greenCreatureCannotBlock() {
        addCreatureReady(player1, new CeruleanWyvern());
        addCreatureReady(player2, new UktabiFaerie());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A non-green flyer can block Cerulean Wyvern")
    void nonGreenFlyerCanBlock() {
        addCreatureReady(player1, new CeruleanWyvern());
        Permanent blocker = addCreatureReady(player2, new BayFalcon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Takes no combat damage from a green creature")
    void takesNoDamageFromGreen() {
        addCreatureReady(player1, new StalkingTiger());
        addCreatureReady(player2, new CeruleanWyvern());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Cerulean Wyvern");
        harness.assertNotOnBattlefield(player1, "Stalking Tiger");
        harness.assertInGraveyard(player1, "Stalking Tiger");
    }

    @Test
    @DisplayName("Cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        Permanent wyvern = addCreatureReady(player2, new CeruleanWyvern());

        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, wyvern.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by a red spell")
    void canBeTargetedByRedSpell() {
        Permanent wyvern = addCreatureReady(player1, new CeruleanWyvern());

        harness.setHand(player1, List.of(new SpittingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, wyvern.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Spitting Earth");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(wyvern.getId());
    }
}
