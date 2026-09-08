package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.d.DarkProphecy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeysaKarlovTest extends BaseCardTest {

    @Test
    void doublesTriggeredAbilitiesCausedByCreatureDeath() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void doublesDeathTriggersEvenWhenTeysaDiesAtTheSameTime() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.w.WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    void grantsVigilanceAndLifelinkToCreatureTokens() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID travelerId = harness.getPermanentId(player1, "Doomed Traveler");
        harness.castInstant(player2, 0, travelerId);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    void doesNotDoubleUnrelatedTriggeredAbilities() {
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
