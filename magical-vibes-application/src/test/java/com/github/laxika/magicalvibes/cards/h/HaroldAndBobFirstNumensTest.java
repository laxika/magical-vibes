package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaroldAndBobFirstNumens.class, DoomBlade.class, Forest.class})
class HaroldAndBobFirstNumensTest extends BaseCardTest {

    @Test
    @DisplayName("When Harold and Bob dies as a creature, it returns as an Aura attached to a Forest you control")
    void returnsAsAuraAttachedToForestYouControl() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new HaroldAndBobFirstNumens());

        destroyHaroldAndBob();

        Permanent aura = findPermanent(player1, "Harold and Bob, First Numens");
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
        assertThat(aura.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The returned Aura gives its enchanted Forest three mana and two rad counters")
    void forestAbilityAddsManaAndRadCounters() {
        Permanent forest = returnAttachedToForest();
        harness.activateAbility(player1, battlefieldIndex(forest), 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without a Forest to enchant, Harold and Bob remains in its graveyard")
    void staysInGraveyardWithoutForest() {
        harness.addToBattlefield(player1, new HaroldAndBobFirstNumens());

        destroyHaroldAndBob();

        harness.assertInGraveyard(player1, "Harold and Bob, First Numens");
        harness.assertNotOnBattlefield(player1, "Harold and Bob, First Numens");
    }

    private Permanent returnAttachedToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new HaroldAndBobFirstNumens());
        destroyHaroldAndBob();
        return forest;
    }

    private void destroyHaroldAndBob() {
        Permanent source = findPermanent(player1, "Harold and Bob, First Numens");
        harness.setHand(player2, java.util.List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
