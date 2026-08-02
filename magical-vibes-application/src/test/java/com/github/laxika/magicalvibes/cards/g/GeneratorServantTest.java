package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorServantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Generator Servant adds {C}{C}")
    void activatingAddsTwoColorless() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new GeneratorServant());
        servant.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(findPermanents(player1, "Generator Servant")).isEmpty();
    }

    @Test
    @DisplayName("A creature spell paid for with Generator Servant's mana gains haste")
    void creatureCastWithServantManaGainsHaste() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new GeneratorServant());
        servant.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with ordinary mana does not gain haste")
    void creatureCastWithOrdinaryManaHasNoHaste() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Servant mana spent on a noncreature spell does not carry haste to a later creature spell")
    void manaSpentOnNoncreatureSpellDoesNotGrantHasteLater() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new GeneratorServant());
        servant.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
    }
}
